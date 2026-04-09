package org.dachuang_team.dc_backend_services.domain.VO;

import org.dachuang_team.dc_backend_services.domain.PO.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderVO {

    private String orderId;
    private String buyerId;
    private String sellerId;
    private String status;
    private BigDecimal totalAmount;
    private String trackingNo;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private List<OrderItemVO> items;

    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
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
        return vo;
    }

    public String getOrderId() { return orderId; }
    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getTrackingNo() { return trackingNo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<OrderItemVO> getItems() { return items; }
}
