package org.dachuang_team.dc_backend_services.domain.VO;

import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderVO {

    private Long orderId;
    private String orderNumber;
    private Long buyerId;
    private Long sellerId;
    private String status;
    private BigDecimal totalAmount;
    private String trackingNo;
    private String shippingMethod;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private LocalDateTime completedAt;
    private List<OrderItemVO> items;
    private Boolean hasPartialRefund;
    private Boolean hasPendingRefund = false;
    private Boolean hasRefund = false;
    private BigDecimal approvedRefundAmount = BigDecimal.ZERO;
    private String address;

    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
        vo.orderNumber = order.getOrderNumber();
        vo.orderId = order.getId();
        vo.buyerId = order.getBuyerId();
        vo.sellerId = order.getSellerId();
        vo.status = order.getStatus().name();
        vo.totalAmount = order.getTotalAmount();
        vo.shippingMethod = order.getShippingMethod();
        vo.trackingNo = order.getTrackingNo();
        vo.createdAt = order.getCreatedAt();
        vo.confirmedAt = order.getConfirmedAt();
        vo.shippedAt = order.getShippedAt();
        vo.receivedAt = order.getReceivedAt();
        vo.paidAt = order.getPaidAt();
        vo.completedAt = order.getCompletedAt();
        vo.items = order.getItems().stream()
                .map(OrderItemVO::from)
                .collect(Collectors.toList());
        vo.hasPartialRefund = order.getHasPartialRefund();
        vo.address = order.getReceiveAddress();
        return vo;
    }

    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public Long getBuyerId() { return buyerId; }
    public Long getSellerId() { return sellerId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getTrackingNo() { return trackingNo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<OrderItemVO> getItems() { return items; }
    public String getAddress() { return address; }
    public Boolean getHasPartialRefund() {
        return hasPartialRefund;
    }
    public void setHasPartialRefund(Boolean hasPartialRefund) {
        this.hasPartialRefund = hasPartialRefund;
    }
    public String getShippingMethod() {
        return shippingMethod;
    }
    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
    public BigDecimal getApprovedRefundAmount() {
        return approvedRefundAmount;
    }
    public void setApprovedRefundAmount(BigDecimal approvedRefundAmount) {
        this.approvedRefundAmount = approvedRefundAmount;
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
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
