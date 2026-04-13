package org.dachuang_team.dc_backend_services.services;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;
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
import java.time.format.DateTimeFormatter;
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
        order.setConfirmedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("商家确认订单: 订单号：{}", orderNumber);
        return saved.getOrderNumber();
    }

    // 4.商家发货

     // 商家发货：CONFIRMED - SHIPPING
     // 发货时必须提供物流单号，物流单号不能为空。
    @Transactional
    public Order shipOrder(String orderNumber, Long sellerId, String trackingNo) throws OrderStateException {

        Order order = getOrderAndValidateSeller(orderNumber, sellerId);

        if(order.getHasPendingRefund()) {
            throw new IllegalStateException("订单有待处理的退款申请，请先处理退款后再发货");
        }

        assertStatus(order, OrderStatus.CONFIRMED, "发货");

        if(trackingNo == null || trackingNo.isEmpty()) {
            order.setShippingMethod("OTHER"); // 无须发货
            order.setReceivedAt(LocalDateTime.now()); // 无须发货的订单直接进入已收货状态
            sendEvent(order, OrderEvent.SERVE, null);
        } else {
            order.setShippingMethod("DELIVERY");
            order.setTrackingNo(trackingNo); // 物流发货
            sendEvent(order, OrderEvent.SHIP, null);
        }
        order.setShippedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        log.info("商家发货: orderId={}, trackingNo={}", orderNumber, trackingNo);
        return saved;
    }

    // 5.买家签收（通常来讲是自动的）

     // 买家签收：SHIPPING → RECEIVED
     // 只有买家本人才能签收。
    @Transactional
    public Order receiveOrder(String orderNumber, Long buyerId) {
        Order order = getOrderAndValidateBuyer(orderNumber, buyerId);

        assertStatus(order, OrderStatus.SHIPPING, "签收");

        sendEvent(order, OrderEvent.RECEIVE, null);

        order.setReceivedAt(LocalDateTime.now());
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

        if(order.getHasPendingRefund()) {
            throw new IllegalStateException("订单有待处理的退款申请，请先取消退款申请或联系商家处理退款申请后再确认收货");
        }

        assertStatus(order, OrderStatus.RECEIVED, "确认收货");

        sendEvent(order, OrderEvent.COMPLETE, null);
        order.setCompletedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("买家确认收货，订单完成，订单号：{}",orderNumber);
        return saved;
    }

    // 7.申请退款

    // 根据订单状态和退款类型不同，支持不同的退款类型和金额限制。
    // 这里只校验订单是否属于该买家
    // 退款申请由退款服务处理，不阻塞订单流程，订单状态不变，但标记有待处理退款，商家处理后根据情况驱动状态机进入FULLY_REFUNDED终态。
    @Transactional
    public RefundRequestVO requestRefund(RefundRequestDTO request, Long buyerId)
            throws OrderStateException {

        if (refundRequestRepository.existsByOrderNumberAndStatus(
                request.getOrderNumber(), RefundStatus.PENDING)){
            throw new IllegalStateException("已有未处理的退款申请，请勿重复提交");
        }
        if(!request.getRefundType().equals("ALL_NO_RT") && !request.getRefundType().equals("ALL_RT") && !request.getRefundType().equals("PARTIAL")) {
            throw new IllegalArgumentException("未知的退款类型: " + request.getRefundType());
        }

        RefundRequest refundRequest = new RefundRequest();
        Order order = getOrderAndValidateBuyer(request.getOrderNumber(), buyerId);
        OrderStatus status = order.getStatus();

        // 终态订单不允许申请退款
        if (status == OrderStatus.COMPLETED
                || status == OrderStatus.FULLY_REFUNDED
                || status == OrderStatus.CANCELLED) {
            throw new OrderStateException("当前订单状态不允许申请退款");
        }

        // PS：非签收状态下应该允许退货退款，反正后面都是走退款的状态流转

        // 查询该订单已通过的退款总额
        BigDecimal refundedTotal = order.getApprovedRefundAmount();
        if (refundedTotal == null) refundedTotal = BigDecimal.ZERO;
        BigDecimal actualAmount;

        // 发货前，申请全额仅退款直接通过
        if (status == OrderStatus.PAID || status == OrderStatus.CONFIRMED) {
            if(!Objects.equals(request.getRefundType(), "ALL_NO_RT")) {
                throw new IllegalArgumentException("未发货订单请直接申请全额仅退款");
            }
            if (refundedTotal.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException(
                        String.format("该订单已有退款通过记录（累计 %s），不能再申请全额退款",
                                refundedTotal));
            }

            actualAmount = order.getTotalAmount();
            refundRequest.setRefundAmount(actualAmount);
            order.addApprovedRefundAmount(actualAmount);
            order.setAutoRefund(true);
            order.setHasRefund(true);
            for (OrderItem item : order.getItems()) {
                productRepository.incrementStock(item.getProductId(), item.getQuantity());
            }
            // 直接退款
            paymentProvider.refund(order, actualAmount);
            refundRequest.setStatus(RefundStatus.AUTO_APPROVED);
            refundRequest.setLastHandleTime(LocalDateTime.now());
            sendEvent(order, OrderEvent.FULLY_REFUND, null);
            log.info("未发货订单申请全额退款，自动批准: orderNumber={}, refundNo={}, buyerId={}, amount={}",
                    request.getOrderNumber(), refundRequest.getRefundNo(), buyerId, actualAmount);

        //  已发货，申请退款需要商家审核
        } else {
            if (Objects.equals(request.getRefundType(), "ALL_RT") || Objects.equals(request.getRefundType(), "ALL_NO_RT")) {
                if (refundedTotal.compareTo(BigDecimal.ZERO) > 0) {
                    throw new IllegalArgumentException(
                            String.format("该订单已有退款通过记录（累计 %s），不能再申请全额退款",
                                    refundedTotal));
                }

                actualAmount = order.getTotalAmount();

            } else if (Objects.equals(request.getRefundType(), "PARTIAL")) {
                if (request.getRefundAmount() == null
                        || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("部分退款金额必须大于 0");
                }
                if (request.getRefundAmount().compareTo(order.getTotalAmount()) >= 0) {
                    throw new IllegalArgumentException("部分退款金额必须小于订单总金额");
                }
                if (request.getRefundAmount().add(refundedTotal)
                        .compareTo(order.getTotalAmount()) > 0) {
                    throw new IllegalArgumentException(
                            String.format("退款金额超限：订单总额 %s，已退款 %s，本次最多可申请 %s",
                                    order.getTotalAmount(),
                                    refundedTotal,
                                    order.remainingRefundable()));
                }
                actualAmount = request.getRefundAmount();
                // 标记订单有待处理退款
                order.setHasPendingRefund(true);
                log.info("买家申请退款: orderNumber={}, refundNo={}, buyerId={}, amount={}",
                        request.getOrderNumber(), refundRequest.getRefundNo(), buyerId, actualAmount);
            } else {
                throw new IllegalArgumentException("未知的退款类型: " + request.getRefundType());
            }
            refundRequest.setStatus(RefundStatus.PENDING);
        }

        refundRequest.setRefundNo(generateRefundNo());
        refundRequest.setOrderNumber(order.getOrderNumber());
        refundRequest.setBuyerId(buyerId);
        refundRequest.setSellerId(order.getSellerId());
        refundRequest.setRefundType(request.getRefundType());
        refundRequest.setRefundAmount(actualAmount);
        refundRequest.setOrderTotalAmount(order.getTotalAmount());
        refundRequest.setReason(request.getReason());
        refundRequest.setRequestTime(LocalDateTime.now());
        refundRequest.setImages(request.getImageIds() == null
                ? Collections.emptyList()
                : request.getImageIds().stream()
                .map(id -> {
                    RefundImg img = refundImgRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "退款凭证图片不存在: " + id));
                    img.setRefundRequest(refundRequest);
                    img.setLinked(true);
                    img.setOrderNumber(order.getOrderNumber());
                    return img;
                })
                .toList());

        refundRequestRepository.save(refundRequest);
        orderRepository.save(order);

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }

    // 商家处理退款
    // 分为部分退款，退货退款和仅退款三种类型，商家同意部分退款时订单状态不变，同意全额退款时订单进入FULLY_REFUNDED终态（无论是否退货）
    @Transactional
    public RefundRequestVO processRefund(String refundNo, Long sellerId,
                                         boolean approve, String reason)
            throws OrderStateException {

        RefundRequest refundRequest = refundRequestRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new IllegalArgumentException("退款申请不存在"));

        Order order = getOrderAndValidateSeller(refundRequest.getOrderNumber(), sellerId);

        if (!refundRequest.getSellerId().equals(sellerId)) {
            throw new OrderAccessDeniedException("无权处理此退款申请");
        }
        if (!RefundStatus.PENDING.equals(refundRequest.getStatus())){
            throw new IllegalStateException("退款申请已处理，请勿重复处理");
        }

        if (approve) {

            if ("PARTIAL".equals(refundRequest.getRefundType())) {
                // 部分退款：订单状态不变，更新累计退款金额和标记
                order.addApprovedRefundAmount(refundRequest.getRefundAmount());
                order.setHasRefund(true);
                paymentProvider.refund(order, refundRequest.getRefundAmount());
                log.info("商家同意部分退款: refundNo={}, amount={}, 订单状态维持: {}",
                        refundNo, refundRequest.getRefundAmount(), order.getStatus());
            } else if ("ALL_NO_RT".equals(refundRequest.getRefundType())) {
                // 全额退款（仅退款）：订单进入 FULLY_REFUNDED 终态，无需退货
                sendEvent(order, OrderEvent.FULLY_REFUND, null);
                order.addApprovedRefundAmount(refundRequest.getRefundAmount());
                order.setHasRefund(true);
                for (OrderItem item : order.getItems()) {
                    productRepository.incrementStock(item.getProductId(), item.getQuantity());
                }
                // 直接退款
                paymentProvider.refund(order, refundRequest.getRefundAmount());
                refundRequest.setStatus(RefundStatus.APPROVED);
                log.info("商家同意全额退款: refundNo={}, 订单进入终态 FULLY_REFUNDED", refundNo);
            } else if("ALL_RT".equals(refundRequest.getRefundType())) {
                // 全额退款（退货退款），退款流程进从PENDING变更为PENDING_RETURN，等待买家提交退货物流单号
                order.addApprovedRefundAmount(refundRequest.getRefundAmount());
                order.setHasRefund(true);
                refundRequest.setStatus(RefundStatus.PENDING_RETURN);
                // PS：在自动化确认收货的功能实现时，需要检查当前订单下是否还有不为REJECTED / CANCELLED / APPROVED / AUTO_APPROVED / REFUNDED的退款申请
                // 如果有则不自动确认收货，直到这些退款申请都处理完毕。
                log.info("商家同意全额退款（退货退款）: refundNo={}", refundNo);
            } else {
                throw new IllegalArgumentException("未知的退款类型: " + refundRequest.getRefundType());
            }

            refundRequest.setLastHandleTime(LocalDateTime.now());

        } else {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("拒绝退款必须提供理由");
            }
            // 拒绝退款,订单状态不变
            refundRequest.setStatus(RefundStatus.REJECTED);
            refundRequest.setRejectReason(reason);
            refundRequest.setLastHandleTime(LocalDateTime.now());
            log.info("商家拒绝退款: refundNo={}, orderNumber={}",
                    refundNo, refundRequest.getOrderNumber());
        }

        refundRequestRepository.save(refundRequest);

        // 检查是否还有其他 PENDING 退款申请，没有则清除标记
        boolean stillHasPending = refundRequestRepository
                .existsByOrderNumberAndStatus(refundRequest.getOrderNumber(), RefundStatus.PENDING);
        order.setHasPendingRefund(stillHasPending);
        orderRepository.save(order);

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }


    @Transactional
    public RefundRequestVO cancelRefund(String refundNo, Long buyerId)
            throws OrderStateException {

        RefundRequest refundRequest = refundRequestRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new IllegalArgumentException("退款申请不存在"));

        Order order = getOrderAndValidateBuyer(refundRequest.getOrderNumber(), buyerId);

        // 非终态均可撤销
        if (RefundStatus.APPROVED.equals(refundRequest.getStatus()) ||
            RefundStatus.REJECTED.equals(refundRequest.getStatus()) ||
            RefundStatus.CANCELLED.equals(refundRequest.getStatus()) ||
            RefundStatus.REFUNDED.equals(refundRequest.getStatus()) ||
            RefundStatus.AUTO_APPROVED.equals(refundRequest.getStatus())
        ) {
            throw new IllegalStateException("退款申请已处理，无法撤销");
        }

        // 撤销退款
        refundRequest.setStatus(RefundStatus.CANCELLED);
        refundRequest.setLastHandleTime(LocalDateTime.now());
        refundRequestRepository.save(refundRequest);

        // 检查是否还有其他 PENDING 退款申请，没有则清除标记
        boolean stillHasPending = refundRequestRepository
                .existsByOrderNumberAndStatus(refundRequest.getOrderNumber(), RefundStatus.PENDING);
        order.setHasPendingRefund(stillHasPending);
        orderRepository.save(order);

        log.info("买家撤销退款: refundNo={}, buyerId={}", refundNo, buyerId);

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
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

    // 10 .退货退款流程

    // 买家填写退货物流单号，确认已寄出商品
    // PENDING_RETURN -> RETURNING
    @Transactional
    public RefundRequestVO submitReturnTracking(String refundNo, Long buyerId,
                                                String returnTrackingNo) {

        if (returnTrackingNo == null || returnTrackingNo.isBlank()) {
            throw new IllegalArgumentException("退货物流单号不能为空");
        }

        RefundRequest refundRequest = refundRequestRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new IllegalArgumentException("退款申请不存在"));

        if (!refundRequest.getBuyerId().equals(buyerId)) {
            throw new OrderAccessDeniedException("无权操作此退款申请");
        }

        if(!refundRequest.getRefundType().equals("ALL_RT")) {
            throw new IllegalStateException("当前退款申请不支持退货流，如需退货请取消撤销当前退款并重新申请");
        }

        if(!RefundStatus.PENDING_RETURN.equals(refundRequest.getStatus())) {
            throw new IllegalStateException("当前退款申请状态不允许提交退货物流单号");
        }

        refundRequest.setReturnTrackingNo(returnTrackingNo);
        refundRequest.setStatus(RefundStatus.RETURNING);
        refundRequest.setReturnShippedTime(LocalDateTime.now());
        refundRequestRepository.save(refundRequest);

        log.info("买家填写退货物流: refundNo={}, trackingNo={}", refundNo, returnTrackingNo);

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }

    // 商家确认签收退货（通常来讲是自动的）
    // RETURNING -> RETURN_RECEIVED
    @Transactional
    public RefundRequestVO receiveReturnedProduct(String refundNo, Long sellerId) {
        RefundRequest refundRequest = refundRequestRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new IllegalArgumentException("退款申请不存在"));

        if (!refundRequest.getSellerId().equals(sellerId)) {
            throw new OrderAccessDeniedException("无权操作此退款申请");
        }

        if(!RefundStatus.RETURNING.equals(refundRequest.getStatus())) {
            throw new IllegalStateException("当前退款申请状态不允许签收退件");
        }

        refundRequest.setStatus(RefundStatus.RETURN_RECEIVED);
        refundRequest.setReturnReceivedTime(LocalDateTime.now());
        refundRequestRepository.save(refundRequest);

        log.info("商家确认收到退货: refundNo={}", refundNo);

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }

    // 商家已收到退货，决定是否执行退款
    // 同意：RETURN_RECEIVED -> APPROVED，执行退款。
    // 拒绝：RETURN_RECEIVED -> REJECTED，如商品损坏不符合退货条件。
    @Transactional
    public RefundRequestVO handleRefundAfterReturnReceived(String refundNo, Long sellerId,
                                                 boolean approve, String reason)
            throws OrderStateException {

        RefundRequest refundRequest = refundRequestRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new IllegalArgumentException("退款申请不存在"));

        if (!refundRequest.getSellerId().equals(sellerId)) {
            throw new OrderAccessDeniedException("无权处理此退款申请");
        }
        if (!RefundStatus.RETURN_RECEIVED.equals(refundRequest.getStatus())){
            throw new IllegalStateException("退货尚未签收，请先签收退件后再处理退款");
        }

        Order order = getOrderAndValidateSeller(refundRequest.getOrderNumber(), sellerId);

        if (approve) {
            paymentProvider.refund(order, refundRequest.getRefundAmount());

            sendEvent(order, OrderEvent.FULLY_REFUND, null);
            order.addApprovedRefundAmount(refundRequest.getRefundAmount());
            order.setHasRefund(true);
            for (OrderItem item : order.getItems()) {
                productRepository.incrementStock(item.getProductId(), item.getQuantity());
            }
            log.info("退货退款（全额）完成: refundNo={}, 订单进入终态 FULLY_REFUNDED", refundNo);

            refundRequest.setStatus(RefundStatus.APPROVED);
            refundRequest.setLastHandleTime(LocalDateTime.now());

        } else {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("拒绝退款必须提供理由");
            }
            refundRequest.setStatus(RefundStatus.REJECTED);
            refundRequest.setRejectReason(reason);
            refundRequest.setLastHandleTime(LocalDateTime.now());
            log.info("商家签收后拒绝退款: refundNo={}, reason={}", refundNo, reason);
        }

        refundRequestRepository.save(refundRequest);

        // 更新待处理退款标记
        boolean stillHasPending = refundRequestRepository
                .existsByOrderNumberAndStatus(refundRequest.getOrderNumber(), RefundStatus.PENDING);
        order.setHasPendingRefund(stillHasPending);
        orderRepository.save(order);

        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);
        return vo;
    }

    // 11.查询相关

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

    public String generateRefundNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "RF" + timestamp + random;
    }
}

/**
 * 有两点需要注意
 * 第一：sendEvent()每次都会stop-reset-start状态机，这是因为Spring StateMachine默认单例，多个订单共用同一实例，必须在每次操作前把它重置到当前订单的状态。
 * 第二：assertStatus()和状态机是双重保险：前者提前给出友好提示，后者作为最终兜底，二者不要删掉任何一个。
 */
