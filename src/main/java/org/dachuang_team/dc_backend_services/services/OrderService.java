package org.dachuang_team.dc_backend_services.services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.common.OrderStateInterceptor;
import org.dachuang_team.dc_backend_services.common.OrderStateListener;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateOrderItemReviewDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.RefundRequestDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.OrderItemReview;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.RefundRequest;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.dachuang_team.dc_backend_services.domain.VO.OrderItemReviewVO;
import org.dachuang_team.dc_backend_services.domain.VO.RefundImgVO;
import org.dachuang_team.dc_backend_services.domain.VO.RefundRequestVO;
import org.dachuang_team.dc_backend_services.enumeration.OrderEvent;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateOrderRequestDTO;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.OrderItem;
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;
import org.dachuang_team.dc_backend_services.repository.*;
import org.dachuang_team.dc_backend_services.services.ServiceException.OrderAccessDeniedException;
import org.dachuang_team.dc_backend_services.services.ServiceException.OrderNotFoundException;
import org.dachuang_team.dc_backend_services.services.ServiceException.OrderStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
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

    private final StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;
    private final OrderStateInterceptor interceptor;
    private final OrderStateListener listener;
    private final OrderRepository orderRepository;
    private final PaymentProvider paymentProvider;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private RefundImgRepository refundImgRepository;

    @Autowired
    private ImageProcessUtils imageProcessUtils;

    @Autowired
    private OrderItemReviewRepository orderItemReviewRepository;

    public OrderService(StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory,
                        OrderStateInterceptor interceptor,
                        OrderStateListener listener,
                        OrderRepository orderRepository,
                        PaymentProvider paymentProvider) {
        this.stateMachineFactory = stateMachineFactory;
        this.interceptor = interceptor;
        this.listener = listener;
        this.orderRepository = orderRepository;
        this.paymentProvider = paymentProvider;
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

        if(order.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("订单已超时，请取消后重新下单");
        }

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
    public Order confirmOrder(String orderNumber, Long sellerId) {
        Order order = getOrderAndValidateSeller(orderNumber, sellerId);
        assertStatus(order, OrderStatus.PAID, "确认");

        sendEvent(order, OrderEvent.CONFIRM, null);
        order.setConfirmedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("商家确认订单: 订单号：{}", orderNumber);
        return saved;
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

    // 5.买家签收

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

        // 订单状态可能是SHIPPING（商家发货但未签收）或者RECEIVED（已签收），两者都允许确认收货
        if(order.getStatus().equals(OrderStatus.SHIPPING)) {
            assertStatus(order, OrderStatus.SHIPPING, "确认收货");
        } else {
            assertStatus(order, OrderStatus.RECEIVED, "确认收货");
        }

        sendEvent(order, OrderEvent.COMPLETE, null);

        // 订单完成后，增加销量
        for (OrderItem item : order.getItems()) {
            productRepository.incrementSales(item.getProductId(), item.getQuantity());
        }

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

        List<RefundStatus> terminalStatuses = List.of(
                RefundStatus.APPROVED,
                RefundStatus.REJECTED,
                RefundStatus.AUTO_APPROVED,
                RefundStatus.CANCELLED,
                RefundStatus.REFUNDED
        );

        List<RefundStatus> approvedStatuses = List.of(
                RefundStatus.APPROVED,
                RefundStatus.AUTO_APPROVED,
                RefundStatus.REFUNDED
        );

        if (refundRequestRepository.existsByOrderNumberAndStatusNotIn(request.getOrderNumber(), terminalStatuses)) {
            throw new IllegalStateException("已存在未完成的退款申请，请勿重复申请");
        }

        if(refundRequestRepository.countByOrderNumberAndStatusIn(request.getOrderNumber(), approvedStatuses) >= 3){
            throw new IllegalStateException("每笔订单最多支持3次成功的退款，该订单已达到退款次数上限");
        }

        if(!request.getRefundType().equals("ALL_NO_RT") && !request.getRefundType().equals("ALL_RT") && !request.getRefundType().equals("PARTIAL")) {
            throw new IllegalArgumentException("未知的退款类型: " + request.getRefundType());
        }

        RefundRequest refundRequest = new RefundRequest();
        Order order = getOrderAndValidateBuyer(request.getOrderNumber(), buyerId);
        OrderStatus status = order.getStatus();

        if (request.getImageIds() != null && request.getImageIds().size() > 5) {
            throw new IllegalArgumentException("最多只能上传5张退款凭证图片");
        }

        // 终态订单不允许申请退款
        if (status == OrderStatus.COMPLETED
                || status == OrderStatus.FULLY_REFUNDED
                || status == OrderStatus.CANCELLED) {
            throw new OrderStateException("当前订单状态不允许申请退款");
        }

        //// 非签收状态下应该允许退货退款，反正后面都是走退款的状态流转

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

        String generatedRefundNo = generateRefundNo();

        refundRequest.setRefundNo(generatedRefundNo);
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
                    imageProcessUtils.refundEvidenceImgProcess(img, generatedRefundNo); // 对原图进行压缩，更新url
                    img.setRefundRequest(refundRequest);
                    img.setLinked(true);
                    img.setRefundNo(generatedRefundNo);
                    img.setOrderNumber(order.getOrderNumber());
                    return img;
                })
                .toList());

        refundRequestRepository.save(refundRequest);
        orderRepository.save(order);

        return convertToRefundRequestVO(refundRequest);
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
                order.setHasPartialRefund(true);
                paymentProvider.refund(order, refundRequest.getRefundAmount());
                refundRequest.setStatus(RefundStatus.APPROVED);
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
                refundRequest.setStatus(RefundStatus.PENDING_RETURN);
                //// PS：在自动化确认收货的功能实现时，需要检查当前订单下是否还有不为REJECTED / CANCELLED / APPROVED / AUTO_APPROVED / REFUNDED的退款申请
                // 如果有则不自动确认收货，直到这些退款申请都处理完毕。
                log.info("商家同意全额退款（退货退款）: refundNo={}", refundNo);
            } else {
                throw new IllegalArgumentException("未知的退款类型: " + refundRequest.getRefundType());
            }


        } else {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("拒绝退款必须提供理由");
            }
            // 拒绝退款,订单状态不变
            refundRequest.setStatus(RefundStatus.REJECTED);
            refundRequest.setRejectReason(reason);
            log.info("商家拒绝退款: refundNo={}, orderNumber={}",
                    refundNo, refundRequest.getOrderNumber());
        }

        refundRequestRepository.save(refundRequest);

        // 检查是否还有其他 PENDING 退款申请，没有则清除标记
        boolean stillHasPending = refundRequestRepository
                .existsByOrderNumberAndStatus(refundRequest.getOrderNumber(), RefundStatus.PENDING);
        order.setHasPendingRefund(stillHasPending);
        orderRepository.save(order);

        refundRequest.setLastHandleTime(LocalDateTime.now());

        return convertToRefundRequestVO(refundRequest);
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

        return convertToRefundRequestVO(refundRequest);
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

        return convertToRefundRequestVO(refundRequest);
    }

    // 商家确认签收退货
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

        log.info("商家确认收到退货: refundNo={}", refundNo);

        return convertToRefundRequestVO(refundRequest);
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

    // 11.订单评价相关

    // 买家对订单项进行评价，订单项只能被订单相关的买家评价，且只能评价一次，评价后不可删除不可修改
    @Transactional
    public OrderItemReviewVO createOrderItemReview(CreateOrderItemReviewDTO reviewDTO, Long buyerId) {
        if (reviewDTO == null) {
            throw new IllegalArgumentException("评价参数不能为空");
        }

        String orderNumber = reviewDTO.getOrderNumber();
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("orderNumber不能为空");
        }

        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new OrderNotFoundException("订单不存在: " + orderNumber);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new OrderStateException("仅已完成订单可评价");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new OrderAccessDeniedException("无权评价此订单");
        }

        Long orderItemId = reviewDTO.getOrderItemId();
        if (orderItemId == null) {
            throw new IllegalArgumentException("orderItemId不能为空");
        }

        OrderItem targetItem = order.getItems().stream()
                .filter(item -> orderItemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new OrderAccessDeniedException("订单中不存在此订单项"));

        Integer rating = reviewDTO.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1~5之间");
        }

        String content = reviewDTO.getContent();
        if (content == null || content.isBlank()) {
            content = "该用户未填写评价内容";
        }

        // 重复评价校验
        if (orderItemReviewRepository.existsByOrderItemId(orderItemId)) {
            throw new IllegalStateException("该订单项已评价，不可重复评价");
        }

        OrderItemReview review = new OrderItemReview();
        review.setOrderNumber(order.getOrderNumber());
        review.setOrderItemId(targetItem.getId());
        review.setProductId(targetItem.getProductId());
        review.setSellerId(order.getSellerId());
        review.setBuyerId(order.getBuyerId());
        review.setAnonymous(Boolean.TRUE.equals(reviewDTO.getAnonymous()));
        review.setRating(rating);
        review.setContent(content.trim());
        review.setProductNameSnapshot(targetItem.getProductName());
        review.setCreatedAt(LocalDateTime.now());

        OrderItemReview saved = orderItemReviewRepository.save(review);

        Product product = productRepository.findById(targetItem.getProductId()).orElse(null);
        // 如果商品已被删除，则不更新评分统计信息
        if(product != null) {
            // 更新商品的评分统计信息，评分数量加1，评分总和加上当前评分
            product.setRatingCount(product.getRatingCount() + 1);
            product.setSumRating(product.getSumRating() + rating);
            productRepository.save(product);
        }

        OrderItemReviewVO vo = new OrderItemReviewVO();
        vo.setBuyerId(saved.getBuyerId());
        vo.setProductId(saved.getProductId());
        vo.setAnonymous(saved.getAnonymous());
        vo.setRating(saved.getRating());
        vo.setContent(saved.getContent());
        vo.setProductNameSnapshot(saved.getProductNameSnapshot());
        vo.setCreatedAt(saved.getCreatedAt());
        return vo;
    }

    // 12.查询相关

    // 用户或商家查询订单列表，订单只能被订单相关的买家或卖家访问
    public Page<Order> getOrdersBySearch(String keyWord, String searchType, Long relatedUserId, Pageable pageable) {
        if (keyWord == null || keyWord.isBlank()) {
            throw new IllegalArgumentException("keyWord 不能为空");
        }

        String normalizedType = (searchType == null ? "NO" : searchType.trim().toUpperCase(Locale.ROOT));
        String kw = keyWord.trim();

        return switch (normalizedType) {
            case "NO" -> orderRepository.findByOrderNumberLikeAndUser(kw, relatedUserId, pageable);
            case "KW" -> orderRepository.findByItemKeywordLikeAndUser(kw, relatedUserId, pageable);
            default -> throw new IllegalArgumentException("searchType仅支持KW或NO");
        };
    }



    // 买家获取自己的订单列表，支持按状态筛选
    public Page<Order> getBuyerOrders(Long buyerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        }
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    // 商家获取自己的订单列表，支持按状态筛选
    public Page<Order> getSellerOrders(Long sellerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        }
        return orderRepository.findBySellerId(sellerId, pageable);
    }

    // 商家查询自己的退款列表，支持按状态筛选
    public Page<RefundRequestVO> getSellerRefundRequests(Long sellerId, RefundStatus status, Pageable pageable) {
        Page<RefundRequest> refundRequests;
        if (status != null) {
            refundRequests = refundRequestRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        } else {
            refundRequests = refundRequestRepository.findBySellerId(sellerId, pageable);
        }
        return refundRequests.map(this::convertToRefundRequestVO);
    }

    // 买家查询自己的退款列表，支持按状态筛选
    public Page<RefundRequestVO> getBuyerRefundRequests(Long buyerId, RefundStatus status, Pageable pageable) {
        Page<RefundRequest> refundRequests;
        if (status != null) {
            refundRequests = refundRequestRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        } else {
            refundRequests = refundRequestRepository.findByBuyerId(buyerId, pageable);
        }
        return refundRequests.map(this::convertToRefundRequestVO);
    }

    // 根据订单id查询该订单下的所有退款申请，根据创建时间排序
    public List<RefundRequestVO> getRefundRequestsByOrderNumber(String orderNumber,Long relatedUserId) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if(order == null) {
            throw new OrderNotFoundException("订单不存在: " + orderNumber);
        }
        if(!order.getSellerId().equals(relatedUserId) && !order.getBuyerId().equals(relatedUserId)) {
            throw new OrderAccessDeniedException("无权访问此订单");
        }

        List<RefundRequest> result = refundRequestRepository.findByOrderNumberOrderByRequestTimeDesc(orderNumber);
        return result.stream()
                .map(this::convertToRefundRequestVO)
                .toList();
    }

    // 商家按买家ID查询订单；status为空时查询所有状态
    public Page<Order> getSellerOrdersByBuyerId(Long sellerId, Long buyerId, OrderStatus status, Pageable pageable) {
        if (sellerId == null || buyerId == null) {
            throw new IllegalArgumentException("sellerId 和 buyerId 不能为空");
        }

        if (status != null) {
            return orderRepository.findBySellerIdAndBuyerIdAndStatus(sellerId, buyerId, status, pageable);
        }
        return orderRepository.findBySellerIdAndBuyerId(sellerId, buyerId, pageable);
    }


    // 根据退款申请编号查询退款申请详情
    public Page<RefundRequestVO> getRefundRequestsByRefundNo(String refundNo, Long relatedUserId, Pageable pageable) {
        Page<RefundRequest> page = refundRequestRepository.findByRefundNoLikeAndUser(refundNo, relatedUserId, pageable);
        return page.map(this::convertToRefundRequestVO);
    }


    // 根据商品ID分页查询评价
    public Page<OrderItemReviewVO> getProductReviews(Long productId, Pageable pageable) {
        if (productId == null) {
            throw new IllegalArgumentException("productId 不能为空");
        }
        return orderItemReviewRepository.findByProductId(productId, pageable)
                .map(this::convertToOrderItemReviewVO);
    }

    // 根据用户ID分页查询评价
    public Page<OrderItemReviewVO> getUserReviews(Long buyerId, Pageable pageable) {
        if (buyerId == null) {
            throw new IllegalArgumentException("buyerId 不能为空");
        }
        return orderItemReviewRepository.findByBuyerId(buyerId, pageable)
                .map(this::convertToOrderItemReviewVO);
    }

    // 根据商家ID分页查询该商家商品的评价
    public Page<OrderItemReviewVO> getSellerReviews(Long sellerId, Pageable pageable) {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId 不能为空");
        }
        return orderItemReviewRepository.findBySellerId(sellerId, pageable)
                .map(this::convertToOrderItemReviewVO);
    }

    //13.自动状态流转相关
    // 活跃退款终态集合
    private static final List<RefundStatus> ACTIVE_REFUND_TERMINAL_STATUSES = List.of(
            RefundStatus.REJECTED,
            RefundStatus.CANCELLED,
            RefundStatus.APPROVED,
            RefundStatus.AUTO_APPROVED,
            RefundStatus.REFUNDED
    );


    // 自动签收SHIPPING且shippedAt<=cutoff的订单
    // 只处理autoReceived=false的订单
    @Transactional
    public int autoReceiveExpiredOrders(LocalDateTime cutoff, int batchSize) {
        int success = 0;
        while (true) {
            Page<Order> page = orderRepository.findByStatusAndShippedAtBeforeAndAutoReceivedFalse(
                    OrderStatus.SHIPPING,
                    cutoff,
                    PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "id"))
            );

            if (page.isEmpty()) {
                break;
            }

            for (Order order : page.getContent()) {
                try {
                    sendEvent(order, OrderEvent.RECEIVE, null);
                    order.setReceivedAt(LocalDateTime.now());
                    order.setAutoReceived(true);
                    orderRepository.save(order);
                    success++;
                } catch (Exception e) {
                    log.warn("自动签收失败: orderNumber={}, err={}", order.getOrderNumber(), e.getMessage(), e);
                }
                log.info("自动签收: orderNumber={}, success={}", order.getOrderNumber(), success);
            }
        }
        return success;
    }

    // 自动取消未支付订单：PENDING_PAYMENT且createdAt<=cutoff的订单
    @Transactional
    public int autoCancelExpiredUnpaidOrders(LocalDateTime cutoff, int batchSize) {
        int success = 0;
        while (true) {
            Page<Order> page = orderRepository.findByStatusAndCreatedAtBefore(
                    OrderStatus.PENDING_PAYMENT,
                    cutoff,
                    PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "id"))
            );

            if (page.isEmpty()) {
                break;
            }

            for (Order order : page.getContent()) {
                try {
                    sendEvent(order, OrderEvent.CANCEL, null);
                    order.setAutoCancel(true);
                    // 取消订单后增加库存
                    for(OrderItem item : order.getItems()) {
                        productRepository.incrementStock(item.getProductId(), item.getQuantity());
                    }
                    orderRepository.save(order);
                    success++;
                } catch (Exception e) {
                    log.warn("自动取消订单失败: orderNumber={}, err={}", order.getOrderNumber(), e.getMessage(), e);
                }
                log.info("自动取消订单: orderNumber={}, success={}", order.getOrderNumber(), success);
            }
        }
        return success;
    }

    // 自动确认收货：RECEIVED且receivedAt<=cutoff的订单
    // 只处理autoCompleted=false的订单
    // 使用hasActiveRefundRequests()检查是否存在活跃退款申请，如果存在则跳过自动确认收货，直到这些退款申请都处理完毕
    @Transactional
    public int autoCompleteExpiredOrders(LocalDateTime cutoff, int batchSize) {
        int success = 0;
        while (true) {
            Page<Order> page = orderRepository.findByStatusAndReceivedAtBeforeAndAutoCompletedFalse(
                    OrderStatus.RECEIVED,
                    cutoff,
                    PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "id"))
            );

            if (page.isEmpty()) {
                break;
            }

            for (Order order : page.getContent()) {
                try {
                    // 自动确认收货前，检查是否存在活跃退款
                    if (hasActiveRefundRequests(order.getOrderNumber())) {
                        log.info("跳过自动确认收货(存在活跃退款): orderNumber={}", order.getOrderNumber());
                        continue;
                    }

                    sendEvent(order, OrderEvent.COMPLETE, null);

                    // 完成后增加销量
                    for (OrderItem item : order.getItems()) {
                        productRepository.incrementSales(item.getProductId(), item.getQuantity());
                    }

                    order.setCompletedAt(LocalDateTime.now());
                    order.setAutoCompleted(true);
                    orderRepository.save(order);
                    success++;
                } catch (Exception e) {
                    log.warn("自动确认收货失败: orderNumber={}, err={}", order.getOrderNumber(), e.getMessage(), e);
                }
                log.info("自动确认收货: orderNumber={}, success={}", order.getOrderNumber(), success);
            }
        }
        return success;
    }

    // 自动处理超时未处理的退款申请：PENDING且requestTime<=resolveRefundAutoHandleDeadline()的退款申请
    // 默认48h超时
    // 签收后的仅退款（ALL_NO_RT）申请为72h超时
    @Transactional
    public int autoHandlePendingRefundTimeout(LocalDateTime now, int batchSize) {
        int success = 0;

        while (true) {
            Page<RefundRequest> page = refundRequestRepository.findByStatusOrderByRequestTimeAsc(
                    RefundStatus.PENDING,
                    PageRequest.of(0, batchSize)
            );

            if (page.isEmpty()) {
                break;
            }

            int handledInThisBatch = 0;

            for (RefundRequest refundRequest : page.getContent()) {
                try {
                    if (refundRequest.getRequestTime() == null) {
                        log.warn("退款申请requestTime为空，跳过: refundNo={}", refundRequest.getRefundNo());
                        continue;
                    }

                    Order order = orderRepository.findByOrderNumber(refundRequest.getOrderNumber());
                    if (order == null) {
                        log.warn("退款申请对应订单不存在，跳过: refundNo={}, orderNumber={}",
                                refundRequest.getRefundNo(), refundRequest.getOrderNumber());
                        continue;
                    }

                    LocalDateTime deadline = resolveRefundAutoHandleDeadline(refundRequest, order);
                    if (now.isBefore(deadline)) {
                        break;
                    }

                    autoApproveRefundInternal(order, refundRequest, now);
                    handledInThisBatch++;
                    success++;

                } catch (Exception e) {
                    log.warn("自动处理退款失败: refundNo={}, err={}",
                            refundRequest.getRefundNo(), e.getMessage(), e);
                }
                log.info("自动处理退款: refundNo={}, handledInThisBatch={}, success={}",
                        refundRequest.getRefundNo(), handledInThisBatch, success);
            }

            if (handledInThisBatch == 0) {
                break;
            }
        }
        return success;
    }

    // 活跃退款判断（用于自动确认收货前检查）
    public boolean hasActiveRefundRequests(String orderNumber) {
        return refundRequestRepository.existsByOrderNumberAndStatusNotIn(
                orderNumber,
                ACTIVE_REFUND_TERMINAL_STATUSES
        );
    }

     // 计算退款超时截止时间：
     // 默认 48h
     // 签收后的仅退款(ALL_NO_RT) 72h
    private LocalDateTime resolveRefundAutoHandleDeadline(RefundRequest refundRequest, Order order) {
        LocalDateTime requestTime = refundRequest.getRequestTime();

        boolean signedOnlyRefund = "ALL_NO_RT".equals(refundRequest.getRefundType())
                && order.getReceivedAt() != null
                && !requestTime.isBefore(order.getReceivedAt()); // requestTime >= receivedAt

        return requestTime.plusHours(signedOnlyRefund ? 72 : 48);
    }

    // 自动审批退款内部逻辑并设置autoRefund标记。
    private void autoApproveRefundInternal(Order order, RefundRequest refundRequest, LocalDateTime now)
            throws OrderStateException {

        if (!RefundStatus.PENDING.equals(refundRequest.getStatus())) {
            return;
        }

        String refundType = refundRequest.getRefundType();

        switch (refundType) {
            case "PARTIAL" -> {
                paymentProvider.refund(order, refundRequest.getRefundAmount());
                order.addApprovedRefundAmount(refundRequest.getRefundAmount());
                order.setHasRefund(true);
                order.setHasPartialRefund(true);
                order.setAutoRefund(true);
                refundRequest.setStatus(RefundStatus.AUTO_APPROVED);
            }
            case "ALL_NO_RT" -> {
                paymentProvider.refund(order, refundRequest.getRefundAmount());
                sendEvent(order, OrderEvent.FULLY_REFUND, null);

                order.addApprovedRefundAmount(refundRequest.getRefundAmount());
                order.setHasRefund(true);
                order.setAutoRefund(true);

                for (OrderItem item : order.getItems()) {
                    productRepository.incrementStock(item.getProductId(), item.getQuantity());
                }

                refundRequest.setStatus(RefundStatus.AUTO_APPROVED);
            }
            case "ALL_RT" -> {
                paymentProvider.refund(order, refundRequest.getRefundAmount());
                sendEvent(order, OrderEvent.FULLY_REFUND, null);

                order.addApprovedRefundAmount(refundRequest.getRefundAmount());
                order.setHasRefund(true);
                order.setAutoRefund(true);

                refundRequest.setStatus(RefundStatus.AUTO_APPROVED);
            }
            default -> throw new IllegalArgumentException("未知的退款类型: " + refundType);
        }

        refundRequest.setLastHandleTime(now);
        refundRequestRepository.save(refundRequest);

        boolean stillHasPending = refundRequestRepository.existsByOrderNumberAndStatus(
                order.getOrderNumber(), RefundStatus.PENDING
        );
        order.setHasPendingRefund(stillHasPending);

        orderRepository.save(order);

        log.info("自动处理退款成功: refundNo={}, orderNumber={}, refundType={}, status={}",
                refundRequest.getRefundNo(), order.getOrderNumber(), refundType, refundRequest.getStatus());
    }




    private void sendEvent(Order order, OrderEvent event, String preStatus)
            throws OrderStateException {

        // 每次调用创建一个全新的状态机实例，无并发竞争
        StateMachine<OrderStatus, OrderEvent> sm =
                stateMachineFactory.getStateMachine(UUID.randomUUID().toString());

        // 注册拦截器和监听器
        sm.getStateMachineAccessor().doWithAllRegions(a -> {
            a.addStateMachineInterceptor(interceptor);
            sm.addStateListener(listener);
        });

        // 直接reset到当前订单状态
        sm.getStateMachineAccessor().doWithAllRegions(a ->
                a.resetStateMachineReactively(new DefaultStateMachineContext<>(
                        order.getStatus(), null, null, null)).block()
        );

        sm.startReactively().block();

        // 构造消息
        MessageBuilder<OrderEvent> builder = MessageBuilder
                .withPayload(event)
                .setHeader("orderId", order.getId())
                .setHeader("order", order);

        if (preStatus != null && !preStatus.isBlank()) {
            builder.setHeader("preRefundStatus", preStatus);
        }

        Message<OrderEvent> message = builder.build();

        boolean accepted = Boolean.TRUE.equals(
                sm.sendEvent(Mono.just(message))
                        .map(result -> result.getResultType()
                                == StateMachineEventResult.ResultType.ACCEPTED)
                        .blockFirst()
        );

        // 用完立即释放资源
        sm.stopReactively().block();

        if (!accepted) {
            throw new OrderStateException(
                    String.format("当前状态 [%s] 不允许执行操作 [%s]，请检查订单状态后重试",
                            order.getStatus(), event)
            );
        }

        order.setStatus(sm.getState().getId());
    }

    // 实体类转换辅助方法：将RefundRequest转换为前端需要的RefundRequestVO并手动映射图片列表
    private RefundRequestVO convertToRefundRequestVO(RefundRequest refundRequest) {
        RefundRequestVO vo = new RefundRequestVO();
        BeanUtils.copyProperties(refundRequest, vo);

        if (refundRequest.getImages() != null) {
            List<RefundImgVO> imgVOs = refundRequest.getImages().stream()
                    .map(img -> {
                        RefundImgVO imgVO = new RefundImgVO();
                        imgVO.setId(img.getId());
                        imgVO.setRefundNo(img.getRefundNo());
                        imgVO.setImageUrl(img.getImageUrl());
                        return imgVO;
                    })
                    .toList();
            vo.setImages(imgVOs);
        }
        return vo;
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

    private OrderItemReviewVO convertToOrderItemReviewVO(OrderItemReview review) {
        OrderItemReviewVO vo = new OrderItemReviewVO();
        vo.setBuyerId(review.getBuyerId());
        vo.setProductId(review.getProductId());
        vo.setAnonymous(review.getAnonymous());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setProductNameSnapshot(review.getProductNameSnapshot());
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }
}

//// 注意：assertStatus()和状态机是双重保险：前者提前给出友好提示，后者作为最终兜底，二者不要删掉任何一个
