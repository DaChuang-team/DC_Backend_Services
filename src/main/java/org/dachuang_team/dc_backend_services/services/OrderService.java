package org.dachuang_team.dc_backend_services.services;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.OrderStateInterceptor;
import org.dachuang_team.dc_backend_services.common.OrderStateListener;
import org.dachuang_team.dc_backend_services.enumeration.OrderEvent;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateOrderRequestDTO;
import org.dachuang_team.dc_backend_services.domain.PO.Order;
import org.dachuang_team.dc_backend_services.domain.PO.OrderItem;
import org.dachuang_team.dc_backend_services.repository.OrderRepository;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderAccessDeniedException;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderNotFoundException;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final StateMachine<OrderStatus, OrderEvent> stateMachine;
    private final OrderStateInterceptor interceptor;
    private final OrderStateListener listener;
    private final OrderRepository orderRepository;
    private final PaymentProvider paymentProvider;

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
     * 2. 构建 OrderItem 列表（每个订单项在构造函数内自校验）
     * 3. 构建 Order（构造函数内自动计算 totalAmount）
     * 4. 持久化，初始状态为 PENDING_PAYMENT，无需触发状态机
     * <p>
     * 由于前端只针对单品购买，items 列表里只会有一个 OrderItem，
     * 但 Service 层不做此限制，保留扩展性。
     */
    @Transactional
    public Order createOrder(CreateOrderRequestDTO request) {
        validateCreateRequest(request);

        List<OrderItem> items = request.getItems().stream()
                .map(dto -> new OrderItem(
                        dto.getProductId(),
                        dto.getProductName(),
                        dto.getProductSnapshot(),
                        dto.getUnitPrice(),
                        dto.getQuantity()
                ))
                .collect(Collectors.toList());

        Order order = new Order(request.getBuyerId(), request.getSellerId(), items);
        Order saved = orderRepository.save(order);
        log.info("订单创建成功: orderId={}, buyerId={}, totalAmount={}",
                saved.getId(), saved.getBuyerId(), saved.getTotalAmount());
        return saved;
    }

    // 2.支付（预留）

    /**
     * 支付流程：
     * 1. 查询订单，校验买家身份
     * 2. 触发支付插槽（现阶段为 MockPaymentProvider，后期替换实现即可）
     * 3. 驱动状态机：PENDING_PAYMENT → PAID
     * 4. 记录支付时间
     * <p>
     * 注意：paymentProvider.pay() 和 sendEvent() 都在同一个事务内，
     * 若状态机迁移失败，支付插槽的内存操作也会随事务回滚。
     * 真实支付接入后，需要考虑支付回调的幂等处理（见注释）。
     */
    @Transactional
    public Order payOrder(String orderId, String buyerId) throws OrderStateException {
        Order order = getOrderAndValidateBuyer(orderId, buyerId);
        assertStatus(order, OrderStatus.PENDING_PAYMENT, "支付");

        // 支付插槽调用
        String paymentResult = paymentProvider.pay(order);
        order.setPaymentSlot(paymentResult);

        // 驱动状态机
        sendEvent(order, OrderEvent.PAY);
        order.setPaidAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("订单支付成功: orderId={}", orderId);
        return saved;

        /*
         * 真实支付接入说明（预留）：
         * 真实支付通常是异步回调模式：
         *   1. paymentProvider.pay() 返回一个预支付 ID（如微信的 prepayId）
         *   2. 前端用预支付 ID 拉起收银台
         *   3. 用户完成支付后，第三方回调你的 /payment/callback 接口
         *   4. 在回调接口里调用 payOrder() 完成状态迁移
         * 回调接口需要加幂等保护（如用 paymentSlot 里的 tradeNo 做唯一键去重）。
         */
    }

    // 3.商家确认

    /**
     * 商家确认订单：PAID → CONFIRMED
     * 只有商家本人才能操作，校验 sellerId。
     */
    @Transactional
    public Order confirmOrder(String orderId, String sellerId) {
        Order order = getOrderAndValidateSeller(orderId, sellerId);
        assertStatus(order, OrderStatus.PAID, "确认");

        sendEvent(order, OrderEvent.CONFIRM);

        Order saved = orderRepository.save(order);
        log.info("商家确认订单: orderId={}, sellerId={}", orderId, sellerId);
        return saved;
    }

    // 4.商家发货

    /**
     * 商家发货：CONFIRMED → SHIPPED
     * 发货时必须提供物流单号，物流单号不能为空。
     */
    @Transactional
    public Order shipOrder(String orderId, String sellerId, String trackingNo) throws OrderStateException {
        if (trackingNo == null || trackingNo.isBlank()) {
            throw new IllegalArgumentException("物流单号不能为空");
        }

        Order order = getOrderAndValidateSeller(orderId, sellerId);
        assertStatus(order, OrderStatus.CONFIRMED, "发货");

        order.setTrackingNo(trackingNo);
        sendEvent(order, OrderEvent.SHIP);

        Order saved = orderRepository.save(order);
        log.info("商家发货: orderId={}, trackingNo={}", orderId, trackingNo);
        return saved;
    }

    // 5.买家签收

    /**
     * 买家签收：SHIPPED → RECEIVED
     * 只有买家本人才能签收。
     */
    @Transactional
    public Order receiveOrder(String orderId, String buyerId) {
        Order order = getOrderAndValidateBuyer(orderId, buyerId);
        assertStatus(order, OrderStatus.SHIPPED, "签收");

        sendEvent(order, OrderEvent.RECEIVE);

        Order saved = orderRepository.save(order);
        log.info("买家签收: orderId={}, buyerId={}", orderId, buyerId);
        return saved;
    }

    // 6.买家确认收货（完成）

    /**
     * 买家确认收货：RECEIVED → COMPLETED
     * 确认收货后订单进入终态，不可再发起退款。
     */
    @Transactional
    public Order completeOrder(String orderId, String buyerId) {
        Order order = getOrderAndValidateBuyer(orderId, buyerId);
        assertStatus(order, OrderStatus.RECEIVED, "确认收货");

        sendEvent(order, OrderEvent.COMPLETE);
        order.setCompletedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("买家确认收货，订单完成: orderId={}", orderId);
        return saved;
    }

    // 7.申请退款

    /**
     * 申请退款：PAID / CONFIRMED / SHIPPED / RECEIVED → REFUND_REQUESTED
     * 以上四个状态均可申请，状态机配置中已定义所有合法迁移路径。
     * 这里只校验订单是否属于该买家，具体状态是否合法交给状态机判断。
     */
    @Transactional
    public Order requestRefund(String orderId, String buyerId, String reason) throws OrderStateException {
        Order order = getOrderAndValidateBuyer(orderId, buyerId);

        // 将退款原因写入 paymentSlot（复用 JSON 字段，避免加列）
        appendRefundReason(order, reason);

        sendEvent(order, OrderEvent.REQUEST_REFUND);

        Order saved = orderRepository.save(order);
        log.info("买家申请退款: orderId={}, buyerId={}, reason={}", orderId, buyerId, reason);
        return saved;
    }

    // 8.商家处理退款

    /**
     * 商家同意退款：REFUND_REQUESTED → REFUNDED
     * 商家拒绝退款：REFUND_REQUESTED → CONFIRMED（回到可继续操作的状态）
     *
     * approve=true  时调用支付插槽执行实际退款动作
     * approve=false 时只做状态回退，不调用支付
     */
    @Transactional
    public Order processRefund(String orderId, String sellerId, boolean approve) {
        Order order = getOrderAndValidateSeller(orderId, sellerId);
        assertStatus(order, OrderStatus.REFUND_REQUESTED, "处理退款");

        if (approve) {
            // 支付插槽调用：执行退款
            paymentProvider.refund(order);
            sendEvent(order, OrderEvent.APPROVE_REFUND);
            log.info("商家同意退款: orderId={}", orderId);
        } else {
            sendEvent(order, OrderEvent.REJECT_REFUND);
            log.info("商家拒绝退款: orderId={}", orderId);
        }

        return orderRepository.save(order);
    }

    // 9.取消订单

    /**
     * 取消订单：PENDING_PAYMENT → CANCELLED
     * 仅待支付状态可取消，其他状态需走退款流程。
     * 只有买家本人可以取消。
     */
    @Transactional
    public Order cancelOrder(String orderId, String buyerId) {
        Order order = getOrderAndValidateBuyer(orderId, buyerId);
        assertStatus(order, OrderStatus.PENDING_PAYMENT, "取消");

        sendEvent(order, OrderEvent.CANCEL);

        Order saved = orderRepository.save(order);
        log.info("买家取消订单: orderId={}, buyerId={}", orderId, buyerId);
        return saved;
    }

    // 10.查询

    /** 查询单个订单（含订单项） */
    @Transactional
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("订单不存在: " + orderId));
    }

    /** 买家查询自己的订单列表，支持按状态筛选 */
    @Transactional
    public Page<Order> getBuyerOrders(String buyerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        }
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    /** 商家查询自己的订单列表，支持按状态筛选 */
    @Transactional
    public Page<Order> getSellerOrders(String sellerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        }
        return orderRepository.findBySellerId(sellerId, pageable);
    }


    /**
     * 核心驱动方法：将状态机恢复到订单当前状态，再发送事件。
     *
     * 为什么每次都要 stop → reset → start？
     * Spring StateMachine 默认是单例的，多个订单共用同一个状态机实例。
     * 必须在每次操作前把状态机重置到当前订单的状态，
     * 否则上一个请求留下的状态会影响当前请求。
     */
    private void sendEvent(Order order, OrderEvent event) throws OrderStateException {
        stateMachine.stop();

        stateMachine.getStateMachineAccessor().doWithAllRegions(a ->
                a.resetStateMachine(new DefaultStateMachineContext<>(
                        order.getStatus(), null, null, null))
        );

        stateMachine.start();

        Message<OrderEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("orderId", order.getId())
                .setHeader("order", order)
                .build();

        boolean accepted = stateMachine.sendEvent(message);

        if (!accepted) {
            throw new OrderStateException(
                    String.format("当前状态 [%s] 不允许执行操作 [%s]，请检查订单状态后重试",
                            order.getStatus(), event)
            );
        }

        // 状态机迁移成功后，将新状态同步回订单实体
        order.setStatus(stateMachine.getState().getId());
    }

    // 下单前参数校验
    private void validateCreateRequest(CreateOrderRequestDTO request) {
        if (request.getBuyerId() == null || request.getBuyerId().isBlank()) {
            throw new IllegalArgumentException("buyerId 不能为空");
        }
        if (request.getSellerId() == null || request.getSellerId().isBlank()) {
            throw new IllegalArgumentException("sellerId 不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单至少包含一个订单项");
        }
        // 校验买家和商家不能是同一个人
        if (request.getBuyerId().equals(request.getSellerId())) {
            throw new IllegalArgumentException("买家和商家不能是同一个人");
        }
    }

    // 查询订单并校验买家身份
    private Order getOrderAndValidateBuyer(String orderId, String buyerId) {
        Order order = getOrder(orderId);
        if (!order.getBuyerId().equals(buyerId)) {
            throw new OrderAccessDeniedException("无权操作此订单：非买家");
        }
        return order;
    }

    // 查询订单并校验商家身份
    private Order getOrderAndValidateSeller(String orderId, String sellerId) {
        Order order = getOrder(orderId);
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
    private void appendRefundReason(Order order, String reason) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> slot = new HashMap<>();
            if (order.getPaymentSlot() != null && !order.getPaymentSlot().isBlank()) {
                slot = mapper.readValue(order.getPaymentSlot(),
                        new TypeReference<Map<String, Object>>() {});
            }
            slot.put("refundReason", reason);
            slot.put("refundRequestedAt", LocalDateTime.now().toString());
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
