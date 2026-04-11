package org.dachuang_team.dc_backend_services.services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.OrderStateInterceptor;
import org.dachuang_team.dc_backend_services.common.OrderStateListener;
import org.dachuang_team.dc_backend_services.domain.DTO.RefundRequestDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.RefundRequest;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.dachuang_team.dc_backend_services.domain.VO.RefundRequestVO;
import org.dachuang_team.dc_backend_services.enumeration.OrderEvent;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateOrderRequestDTO;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.OrderItem;
import org.dachuang_team.dc_backend_services.repository.RefundImgRepository;
import org.dachuang_team.dc_backend_services.repository.RefundRequestRepository;
import org.dachuang_team.dc_backend_services.repository.OrderRepository;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderAccessDeniedException;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderNotFoundException;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final StateMachine<OrderStatus, OrderEvent> stateMachine;
    private final OrderStateInterceptor interceptor;
    private final OrderStateListener listener;
    private final OrderRepository orderRepository;
    private final PaymentProvider paymentProvider;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    RefundRequestRepository refundRequestRepository;

    @Autowired
    private RefundImgRepository refundImgRepository;

    public OrderService(StateMachine<OrderStatus, OrderEvent> stateMachine,
                        OrderStateInterceptor interceptor,
                        OrderStateListener listener,
                        OrderRepository orderRepository,
                        PaymentProvider paymentProvider) {
        this.stateMachine = stateMachine;
        this.interceptor = interceptor;
        this.listener = listener;
        this.orderRepository = orderRepository;
        this.paymentProvider = paymentProvider;

        // 注册拦截器和监听器
        this.stateMachine.getStateMachineAccessor().doWithAllRegions(a -> {
            a.addStateMachineInterceptor(interceptor);
            stateMachine.addStateListener(listener);
        });
    }

    // 1.下单

    /**
     * 下单逻辑：
     * 1. 校验请求参数
     * 2. 构建OrderItem列表（每个订单项在构造函数内自校验）
     * 3. 构建Order（构造函数内自动计算totalAmount）
     * 4. 持久化，初始状态为PENDING_PAYMENT，无需触发状态机
     * 由于前端只针对单品购买，items列表里只会有一个OrderItem，
     * 但Service层不做此限制，保留扩展性。
     */
    @Transactional
    public Order createOrder(CreateOrderRequestDTO request,Long buyerId) throws JsonProcessingException {

        Long orderSellerId = null; // 用于记录当前订单统一的卖家ID

        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderRequestDTO.OrderItemDto dto : request.getItems()) {
            Long pid = Long.valueOf(dto.getProductId());

            // 去DB里查真实的完整商品信息
            Product dbProduct = productRepository.findById(pid)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

            // 卖家一致性校验
            if (orderSellerId == null) {
                // 第一个商品，记录下卖家ID
                orderSellerId = dbProduct.getSellerId();
            } else if (!orderSellerId.equals(dbProduct.getSellerId())) {
                // 后续商品，与第一个卖家ID对比发现不一致时拒绝下单
                throw new IllegalArgumentException("不支持跨店合并下单，请分开结算不同商家的商品");
            }

            if(!dbProduct.getApproved()) {
                throw new IllegalArgumentException("商品未上架");
            }

            if(dbProduct.getStock() < dto.getQuantity()) {
                throw new IllegalArgumentException("商品 " + dbProduct.getProductName() + " 库存不足");
            }

            // 以DB的信息为准
            BigDecimal actualPrice = BigDecimal.valueOf(dbProduct.getPrice());

            // 原子扣库存
            int updated = productRepository.decrementStock(pid, dto.getQuantity());
            if (updated == 0) {
                throw new IllegalStateException("商品 " + dbProduct.getProductName() + " 库存不足");
            }

            // 构建商品快照，保存到订单项里。快照里至少要包含单价和商品名称，其他信息可选。
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> snapshotMap = new HashMap<>();
            snapshotMap.put("productId", dbProduct.getProductId());
            snapshotMap.put("productName", dbProduct.getProductName());
            snapshotMap.put("price", dbProduct.getPrice());
            snapshotMap.put("category", dbProduct.getCategory());
            snapshotMap.put("origin", dbProduct.getOrigin());
            snapshotMap.put("tbImageUrl", dbProduct.getTbImageUrl());
            snapshotMap.put("sellerId", dbProduct.getSellerId());
            snapshotMap.put("description", dbProduct.getDescription());
            String productSnapshot = mapper.writeValueAsString(snapshotMap);

            // 使用真实单价和商品名称构造订单明细
            items.add(new OrderItem(
                    dbProduct.getProductId(),
                    dbProduct.getProductName(),
                    productSnapshot,
                    actualPrice,
                    dto.getQuantity()
            ));
        }

        // 此时orderSellerId一定是所有商品公共的sellerId，已验证无冲突
        // order内自动根据最新的items计算总价
        Order order = new Order(buyerId, orderSellerId, items, request.getAddress());
        return orderRepository.save(order);
    }


    // 2.支付（预留）

    /**
     * 支付流程：
     * 1. 查询订单，校验买家身份
     * 2. 触发支付插槽（现阶段为 MockPaymentProvider）
     * 3. 驱动状态机：PENDING_PAYMENT - PAID
     * 4. 记录支付时间
     * 注意：paymentProvider.pay() 和 sendEvent() 都在同一个事务内，
     * 真实支付接入后，需要考虑支付回调的幂等处理（见注释）。
     */
    @Transactional
    public Order payOrder(String orderNumber, Long buyerId) throws OrderStateException {
        Order order = getOrderAndValidateBuyer(orderNumber, buyerId);
        assertStatus(order, OrderStatus.PENDING_PAYMENT, "支付");

        // 支付插槽调用
        String paymentResult = paymentProvider.pay(order);
        order.setPaymentSlot(paymentResult);

        // 驱动状态机
        sendEvent(order, OrderEvent.PAY, null);
        order.setPaidAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("订单支付成功:，订单号：{}", orderNumber);
        return saved;

        /*
         * 真实支付接入说明（预留）：
         * 真实支付通常是异步回调模式：
         *   1. paymentProvider.pay()返回一个预支付ID（如微信的prepayId）
         *   2. 前端用预支付ID拉起收银台
         *   3. 用户完成支付后，第三方回调 /payment/callback 接口
         *   4. 在回调接口里调用payOrder()完成状态迁移
         * 回调接口需要加幂等保护（如用paymentSlot里的tradeNo做唯一键去重）。
         */
    }

    // 3.商家确认

     // 商家确认订单：PAID - CONFIRMED
     // 只有商家本人才能操作，校验 sellerId。
    @Transactional
    public String confirmOrder(String orderNumber, Long sellerId) {
        Order order = getOrderAndValidateSeller(orderNumber, sellerId);
        assertStatus(order, OrderStatus.PAID, "确认");

        sendEvent(order, OrderEvent.CONFIRM, null);

        Order saved = orderRepository.save(order);
        log.info("商家确认订单: 订单号：{}", orderNumber);
        return saved.getOrderNumber();
    }

    // 4.商家发货

     // 商家发货：CONFIRMED - SHIPPED
     // 发货时必须提供物流单号，物流单号不能为空。
    @Transactional
    public Order shipOrder(String orderNumber, Long sellerId, String trackingNo) throws OrderStateException {
        if (trackingNo == null || trackingNo.isBlank()) {
            throw new IllegalArgumentException("物流单号不能为空");
        }

        Order order = getOrderAndValidateSeller(orderNumber, sellerId);
        assertStatus(order, OrderStatus.CONFIRMED, "发货");

        order.setTrackingNo(trackingNo);
        sendEvent(order, OrderEvent.SHIP, null);

        Order saved = orderRepository.save(order);
        log.info("商家发货: orderId={}, trackingNo={}", orderNumber, trackingNo);
        return saved;
    }

    // 5.买家签收

     // 买家签收：SHIPPED → RECEIVED
     // 只有买家本人才能签收。
    @Transactional
    public Order receiveOrder(String orderNumber, Long buyerId) {
        Order order = getOrderAndValidateBuyer(orderNumber, buyerId);
        assertStatus(order, OrderStatus.SHIPPED, "签收");

        sendEvent(order, OrderEvent.RECEIVE, null);

        Order saved = orderRepository.save(order);
        log.info("买家签收: orderId={}, buyerId={}", orderNumber, buyerId);
        return saved;
    }

    // 6.买家确认收货（完成）

     // 买家确认收货：RECEIVED - COMPLETED
     // 确认收货后订单进入终态，不可再发起退款。
    @Transactional
    public Order completeOrder(String orderNumber, Long buyerId) {
        Order order = getOrderAndValidateBuyer(orderNumber, buyerId);
        assertStatus(order, OrderStatus.RECEIVED, "确认收货");

        sendEvent(order, OrderEvent.COMPLETE, null);
        order.setCompletedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("买家确认收货，订单完成，订单号：{}",orderNumber);
        return saved;
    }

    // 7.申请退款

    // 申请退款：PAID / CONFIRMED / SHIPPED / RECEIVED - REFUND_REQUESTED
    // 这里只校验订单是否属于该买家
    @Transactional
    public RefundRequestVO requestRefund(RefundRequestDTO request, Long BuyerId) throws OrderStateException {
        if(refundRequestRepository.findByOrderNumber(request.getOrderNumber()) != null) {
            throw new IllegalStateException("订单已存在未处理的退款申请");
        }
        Order order = getOrderAndValidateBuyer(request.getOrderNumber(), BuyerId);
        String preOrderStatus = String.valueOf(order.getStatus());
        BigDecimal actualAmount;
        if (Objects.equals(request.getRefundType(), "ALL")) {
            if (order.getStatus() != OrderStatus.PAID &&
                    order.getStatus() != OrderStatus.CONFIRMED &&
                    order.getStatus() != OrderStatus.SHIPPED &&
                    order.getStatus() != OrderStatus.RECEIVED) {
                throw new OrderStateException("当前订单状态不允许申请全额退款");
            }
            actualAmount = order.getTotalAmount(); // 全额退款时，直接从订单获取总金额
        } else if (Objects.equals(request.getRefundType(), "PARTIAL")) { // 部分退款
            if (request.getRefundAmount() == null || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("部分退款金额必须大于0");
            }
            if (request.getRefundAmount().compareTo(order.getTotalAmount()) >= 0) {
                throw new IllegalArgumentException("部分退款金额必须小于订单总金额");
            }
            // 部分退款允许在多个状态申请，但都需要校验订单金额
            if (order.getStatus() != OrderStatus.PAID &&
                    order.getStatus() != OrderStatus.CONFIRMED &&
                    order.getStatus() != OrderStatus.SHIPPED &&
                    order.getStatus() != OrderStatus.RECEIVED) {
                throw new OrderStateException("当前订单状态不允许申请部分退款");
            }
            actualAmount = request.getRefundAmount(); //部分退款时，由买家填入金额
        } else {
            throw new IllegalArgumentException("未知的退款类型: " + request.getRefundAmount());
        }

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderNumber(order.getOrderNumber());
        refundRequest.setBuyerId(BuyerId);
        refundRequest.setSellerId(order.getSellerId());
        refundRequest.setRefundType(request.getRefundType());
        refundRequest.setRefundAmount(actualAmount);
        refundRequest.setOrderTotalAmount(order.getTotalAmount());
        refundRequest.setReason(request.getReason());
        refundRequest.setStatus("PENDING");
        refundRequest.setPreRefundStatus(OrderStatus.valueOf(preOrderStatus)); // 记录申请退款前的订单状态，商家拒绝退款时或买家撤销退款时需要回退
        refundRequest.setRequestTime(LocalDateTime.now());

        refundRequest.setImages(request.getImageIds() == null ? Collections.emptyList() : request.getImageIds().stream()
                .map(id -> {
                    RefundImg img = refundImgRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("退款凭证图片不存在: " + id));
                    img.setRefundRequest(refundRequest);
                    img.setLinked(true);
                    img.setOrderNumber(order.getOrderNumber());
                    return img;
                })
                .toList());

        refundRequestRepository.save(refundRequest);

        sendEvent(order, OrderEvent.REQUEST_REFUND, null);

        Order saved = orderRepository.save(order);
        log.info("买家申请退款: orderId={}, buyerId={}, reason={} amount={}", request.getOrderNumber(), BuyerId, request.getReason(), actualAmount);
        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }

    // 8.退款处理

    // 商家同意退款：REFUND_REQUESTED - REFUNDED
    // 商家拒绝退款：REFUND_REQUESTED - 根据退款前状态回退（PAID / CONFIRMED / SHIPPED / RECEIVED）
    // approve=true时调用支付插槽执行实际退款动作
    // approve=false时只做状态回退，不调用支付
    @Transactional
    public RefundRequestVO processRefund(String orderNumber, Long sellerId, boolean approve, String reason) throws OrderStateException {
        Order order = getOrderAndValidateSeller(orderNumber, sellerId);
        assertStatus(order, OrderStatus.REFUND_REQUESTED, "处理退款");
        RefundRequest refundRequest = refundRequestRepository.findByOrderNumber(orderNumber);
        if (refundRequest == null) {
            throw new IllegalArgumentException("退款申请不存在");
        }

        if (approve) {
            // 支付插槽调用：执行退款
            paymentProvider.refund(order);
            sendEvent(order, OrderEvent.APPROVE_REFUND, null);
            for(OrderItem item : order.getItems()) {
                productRepository.incrementStock(item.getProductId(), item.getQuantity());
            }
            refundRequest.setStatus("APPROVED");
            refundRequest.setHandleTime(LocalDateTime.now());
            log.info("商家同意退款: orderId={}", orderNumber);
        } else {
            if(reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("拒绝退款必须提供理由");
            }
            sendEvent(order, OrderEvent.REJECT_REFUND,refundRequest.getPreRefundStatus().toString()); // 拒绝并回退
            refundRequest.setStatus("REJECTED");
            refundRequest.setRejectReason(reason);
            refundRequest.setPreRefundStatus(null); // 商家拒绝退款状态回退后，退款前状态已无意义，如再仍要退款需要重新申请
            refundRequest.setHandleTime(LocalDateTime.now());
            log.info("商家拒绝退款: orderId={}", orderNumber);
        }
        refundRequestRepository.save(refundRequest);
        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }

    // 买家撤销退款
    // 买家撤销退款：REFUND_REQUESTED - 申请退款前的状态（PAID / CONFIRMED / SHIPPED / RECEIVED）
    @Transactional
    public RefundRequestVO cancelRefund(String orderNumber, Long userId) throws OrderStateException {
        Order order = getOrderAndValidateBuyer(orderNumber, userId);
        assertStatus(order, OrderStatus.REFUND_REQUESTED, "撤销退款");
        RefundRequest refundRequest = refundRequestRepository.findByOrderNumber(orderNumber);
        if(refundRequest == null) {
            throw new IllegalArgumentException("退款申请不存在");
        }
        if(refundRequest.getPreRefundStatus() == null) {
            throw new IllegalStateException("退款申请前状态未知，无法撤销");
        }

        String preOrderStatus = refundRequest.getPreRefundStatus().toString();

        sendEvent(order, OrderEvent.CANCEL_REFUND, preOrderStatus); //取消并回退

        refundRequest.setStatus("CANCELLED");
        refundRequestRepository.save(refundRequest);
        refundRequest.setPreRefundStatus(null);
        refundRequest.setHandleTime(LocalDateTime.now());

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        log.info("买家撤销退款: orderId={}, buyerId={}", orderNumber, userId);
        return vo;
    }

    // 9.取消订单

    // 取消订单：PENDING_PAYMENT → CANCELLED
    // 仅待支付状态可取消，其他状态需走退款流程。
    // 只有买家本人可以取消。
    @Transactional
    public Order cancelOrder(String orderNumber, Long buyerId) {
        Order order = getOrderAndValidateBuyer(orderNumber, buyerId);
        assertStatus(order, OrderStatus.PENDING_PAYMENT, "取消");

        sendEvent(order, OrderEvent.CANCEL, null);
        for(OrderItem item : order.getItems()) {
            productRepository.incrementStock(item.getProductId(), item.getQuantity());
        }
        Order saved = orderRepository.save(order);
        log.info("买家取消订单: orderId={}, buyerId={}", orderNumber, buyerId);
        return saved;
    }

    // 10.查询

    // 查询单个订单（含订单项）
    @Transactional
    public Order getOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if(order == null) {
            throw new OrderNotFoundException("订单不存在: " + orderNumber);
        }
         return order;
    }

    // 买家查询自己的订单列表，支持按状态筛选
    @Transactional
    public Page<Order> getBuyerOrders(Long buyerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        }
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    // 商家查询自己的订单列表，支持按状态筛选
    @Transactional
    public Page<Order> getSellerOrders(Long sellerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        }
        return orderRepository.findBySellerId(sellerId, pageable);
    }



    // 将状态机恢复到订单当前状态，再发送事件
    private void sendEvent(Order order, OrderEvent event, String preStatus) throws OrderStateException {
        stateMachine.stopReactively().block();

        stateMachine.getStateMachineAccessor().doWithAllRegions(a ->
                a.resetStateMachineReactively(new DefaultStateMachineContext<>(
                        order.getStatus(), null, null, null)).block()
        );

        stateMachine.startReactively().block();

        MessageBuilder<OrderEvent> builder = MessageBuilder
                .withPayload(event)
                .setHeader("orderId", order.getId())
                .setHeader("order", order);

        if (preStatus != null && !preStatus.isBlank()) {
            builder.setHeader("preRefundStatus", preStatus);
        }

        Message<OrderEvent> message = builder.build();

        boolean accepted = Boolean.TRUE.equals(
                stateMachine.sendEvent(Mono.just(message))
                        .map(result -> result.getResultType() == StateMachineEventResult.ResultType.ACCEPTED)
                        .blockFirst()
        );

        if (!accepted) {
            throw new OrderStateException(
                    String.format("当前状态 [%s] 不允许执行操作 [%s]，请检查订单状态后重试",
                            order.getStatus(), event)
            );
        }

        // 状态机迁移成功后，将新状态同步回订单实体
        order.setStatus(stateMachine.getState().getId());
    }

    // 查询订单并校验买家身份
    private Order getOrderAndValidateBuyer(String orderNumber, Long buyerId) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new OrderNotFoundException("订单不存在: " + orderNumber);
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new OrderAccessDeniedException("无权操作此订单：非买家");
        }
        return order;
    }

    // 查询订单并校验商家身份
    private Order getOrderAndValidateSeller(String orderNumber, Long sellerId) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new OrderNotFoundException("订单不存在: " + orderNumber);
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new OrderAccessDeniedException("无权操作此订单：非商家");
        }
        return order;
    }

    // 状态错误处理
    private void assertStatus(Order order, OrderStatus expected, String action) {
        if (order.getStatus() != expected) {
            throw new OrderStateException(
                    String.format("订单当前状态为 [%s]，无法执行 [%s] 操作，需要状态为 [%s]",
                            order.getStatus(), action, expected)
            );
        }
    }

    // 退款原因直接写入PaymentSlot
    private void appendRefundReason(Order order, String reason,BigDecimal amount) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> slot = new HashMap<>();
            if (order.getPaymentSlot() != null && !order.getPaymentSlot().isBlank()) {
                slot = mapper.readValue(order.getPaymentSlot(),
                        new TypeReference<Map<String, Object>>() {});
            }
            slot.put("refundReason", reason);
            slot.put("refundRequestedAt", LocalDateTime.now().toString());
            slot.put("amount", amount);
            order.setPaymentSlot(mapper.writeValueAsString(slot));
        } catch (Exception e) {
            log.warn("写入退款原因失败，忽略: {}", e.getMessage());
        }
    }
}

/**
 * 有三点需要特别留意。
 * 第一：sendEvent()每次都会stop-reset-start状态机，这是因为Spring StateMachine默认单例，多个订单共用同一实例，必须在每次操作前把它重置到当前订单的状态。
 * 第二：assertStatus()和状态机是双重保险：前者提前给出友好提示，后者作为最终兜底，二者不要删掉任何一个。
 * 第三：退款原因复用了paymentSlot的JSON字段，前期不需要加列，后期如果退款场景变复杂可以单独建一张order_refund_records表。
 */
