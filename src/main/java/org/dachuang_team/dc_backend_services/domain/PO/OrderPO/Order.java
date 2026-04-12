package org.dachuang_team.dc_backend_services.domain.PO.OrderPO;

import jakarta.persistence.*;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    //订单id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    //买家ID，关联用户表，只存引用
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    //商家ID，关联商家表，只存引用
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    //定义见枚举类
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    //订单总金额，单位：元，精度保留2位小数
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_method")
    private String shippingMethod; // 发货方式：DELIVERY(配送) 或 OTHER(无须发货)，发货时由系统自动填入

    //物流单号，发货后由商家填入。初始null
    @Column(name = "tracking_no", length = 64)
    private String trackingNo;

    //支付预留字段，存储第三方支付返回的上下文信息
    @Column(name = "payment_slot", columnDefinition = "TEXT")
    private String paymentSlot;

    @Column(name = "receive_address", nullable = false)
    private String receiveAddress;

    //乐观锁版本号防止并发修改
    @Version
    private Integer version;

    //创建时间，自动填充
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //更新时间，每次save自动更新
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //支付完成时间
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "has_partial_refund")
    private Boolean hasPartialRefund = false;

    // 是否有待处理的退款申请
    // 申请退款时置为 true，退款申请完结（通过/拒绝/撤销）后置为 false
    // 发货、确认收货等关键节点前检查此字段，提示商家或买家有未处理的退款
    @Column(name = "has_pending_refund", nullable = false)
    private Boolean hasPendingRefund = false;

    // 是否发生过退款（包括部分退款和全额退款），只要有任意一笔退款被批准过就置为 true，不可逆
    @Column(name = "has_refund", nullable = false)
    private Boolean hasRefund = false;

    // 已通过的退款总金额，每次退款批准后累加，退款申请撤销或拒绝时不变。初始0，精度保留2位小数
    @Column(name = "approved_refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal approvedRefundAmount = BigDecimal.ZERO;

    //订单完成时间，买家确认收货填入
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    //订单项列表，保存订单时自动保存订单项，删除订单时自动删除订单项。
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "auto_refund", nullable = false)
    private Boolean autoRefund = false; //自动退款标记

    @Column(name = "auto_received", nullable = false)
    private Boolean autoReceived = false; // 自动签收标记

    @Column(name = "auto_completed", nullable = false)
    private Boolean autoCompleted = false; // 自动完成标记

    protected Order() {}

    // 强制校验
    public Order(Long buyerId, Long sellerId, List<OrderItem> items, String address) {
        this.orderNumber = System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.setItems(items);
        this.totalAmount = calculateTotal();
        this.receiveAddress = address;
        this.hasPartialRefund = false;
        this.autoRefund = false;
        this.autoReceived = false;
        this.autoCompleted = false;
    }

    // 计算总金额
    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //设置订单项时同步建立双向关联
    public void setItems(List<OrderItem> items) {
        this.items = items;
        items.forEach(item -> item.setOrder(this));
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getBuyerId() { return buyerId; }
    public Long getSellerId() { return sellerId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getPaymentSlot() { return paymentSlot; }
    public void setPaymentSlot(String paymentSlot) { this.paymentSlot = paymentSlot; }
    public Integer getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public String getReceiveAddress() {
        return receiveAddress;
    }
    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }
    public Boolean getHasPartialRefund() {
        return hasPartialRefund;
    }
    public void setHasPartialRefund(Boolean hasPartialRefund) {
        this.hasPartialRefund = hasPartialRefund;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Boolean getAutoCompleted() {
        return autoCompleted;
    }
    public void setAutoCompleted(Boolean autoCompleted) {
        this.autoCompleted = autoCompleted;
    }
    public Boolean getAutoReceived() {
        return autoReceived;
    }
    public void setAutoReceived(Boolean autoReceived) {
        this.autoReceived = autoReceived;
    }
    public Boolean getAutoRefund() {
        return autoRefund;
    }
    public void setAutoRefund(Boolean autoRefund) {
        this.autoRefund = autoRefund;
    }
    public String getShippingMethod() {
        return shippingMethod;
    }
    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
    public Boolean getHasRefund() {
        return hasRefund;
    }
    public void setHasRefund(Boolean hasRefund) {
        this.hasRefund = hasRefund;
    }
    public Boolean getHasPendingRefund() {
        return hasPendingRefund;
    }
    public void setHasPendingRefund(Boolean hasPendingRefund) {
        this.hasPendingRefund = hasPendingRefund;
    }
    public BigDecimal getApprovedRefundAmount() {
        return approvedRefundAmount;
    }
    public void addApprovedRefundAmount(BigDecimal amount) {
        this.approvedRefundAmount = this.approvedRefundAmount.add(amount);
    }

    public BigDecimal remainingRefundable() {
        return this.totalAmount.subtract(this.approvedRefundAmount);
    }
}
