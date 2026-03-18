package org.dachuang_team.dc_backend_services.pojo.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    public static class PreviewRequest {
        private String itemType;
        private Long itemId;
        private Integer quantity;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer roomCount;
        private String deliveryAddress;

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public LocalDate getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(LocalDate checkInDate) {
            this.checkInDate = checkInDate;
        }

        public LocalDate getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(LocalDate checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public Integer getRoomCount() {
            return roomCount;
        }

        public void setRoomCount(Integer roomCount) {
            this.roomCount = roomCount;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }
    }

    public static class CreateRequest {
        private String itemType;
        private Long itemId;
        private Integer quantity;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer roomCount;
        private String deliveryAddress;
        private String priceVersion;
        private String clientRequestId;

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public LocalDate getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(LocalDate checkInDate) {
            this.checkInDate = checkInDate;
        }

        public LocalDate getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(LocalDate checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public Integer getRoomCount() {
            return roomCount;
        }

        public void setRoomCount(Integer roomCount) {
            this.roomCount = roomCount;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }

        public String getPriceVersion() {
            return priceVersion;
        }

        public void setPriceVersion(String priceVersion) {
            this.priceVersion = priceVersion;
        }

        public String getClientRequestId() {
            return clientRequestId;
        }

        public void setClientRequestId(String clientRequestId) {
            this.clientRequestId = clientRequestId;
        }
    }

    public static class PayRequest {
        private String payChannel;
        private String payToken;

        public String getPayChannel() {
            return payChannel;
        }

        public void setPayChannel(String payChannel) {
            this.payChannel = payChannel;
        }

        public String getPayToken() {
            return payToken;
        }

        public void setPayToken(String payToken) {
            this.payToken = payToken;
        }
    }

    public static class OrderItemView {
        private String itemType;
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private Integer roomCount;
        private Integer nightCount;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Double unitPrice;
        private Double lineAmount;

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getRoomCount() {
            return roomCount;
        }

        public void setRoomCount(Integer roomCount) {
            this.roomCount = roomCount;
        }

        public Integer getNightCount() {
            return nightCount;
        }

        public void setNightCount(Integer nightCount) {
            this.nightCount = nightCount;
        }

        public LocalDate getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(LocalDate checkInDate) {
            this.checkInDate = checkInDate;
        }

        public LocalDate getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(LocalDate checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Double getLineAmount() {
            return lineAmount;
        }

        public void setLineAmount(Double lineAmount) {
            this.lineAmount = lineAmount;
        }
    }

    public static class PreviewResponse {
        private String itemType;
        private Long itemId;
        private Double totalAmount;
        private String priceVersion;
        private List<OrderItemView> items;

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getPriceVersion() {
            return priceVersion;
        }

        public void setPriceVersion(String priceVersion) {
            this.priceVersion = priceVersion;
        }

        public List<OrderItemView> getItems() {
            return items;
        }

        public void setItems(List<OrderItemView> items) {
            this.items = items;
        }
    }

    public static class OrderSummaryResponse {
        private Long orderId;
        private String orderNo;
        private Integer status;
        private Double payAmount;
        private LocalDateTime createdAt;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Double getPayAmount() {
            return payAmount;
        }

        public void setPayAmount(Double payAmount) {
            this.payAmount = payAmount;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class OrderDetailResponse {
        private Long orderId;
        private String orderNo;
        private Integer status;
        private Double totalPrice;
        private Double payAmount;
        private Double discountAmount;
        private String deliveryAddress;
        private LocalDateTime createdAt;
        private LocalDateTime paidAt;
        private List<OrderItemView> items;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(Double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public Double getPayAmount() {
            return payAmount;
        }

        public void setPayAmount(Double payAmount) {
            this.payAmount = payAmount;
        }

        public Double getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(Double discountAmount) {
            this.discountAmount = discountAmount;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
        }

        public List<OrderItemView> getItems() {
            return items;
        }

        public void setItems(List<OrderItemView> items) {
            this.items = items;
        }
    }

    public static class PayResponse {
        private Long orderId;
        private Integer status;
        private LocalDateTime paidAt;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public LocalDateTime getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
        }
    }

    public static class CancelResponse {
        private Long orderId;
        private Integer status;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
