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
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private List<OrderItemVO> items;
    private String address;

    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
        vo.orderNumber = order.getOrderNumber();
        vo.orderId = order.getId();
        vo.buyerId = order.getBuyerId();
        vo.sellerId = order.getSellerId();
        vo.status = order.getStatus().name();
        vo.totalAmount = order.getTotalAmount();
        vo.trackingNo = order.getTrackingNo();
        vo.createdAt = order.getCreatedAt();
        vo.paidAt = order.getPaidAt();
        vo.completedAt = order.getCompletedAt();
        vo.items = order.getItems().stream()
                .map(OrderItemVO::from)
                .collect(Collectors.toList());
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
}
