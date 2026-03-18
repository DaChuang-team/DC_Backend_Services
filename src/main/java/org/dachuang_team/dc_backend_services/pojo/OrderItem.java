package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private Double priceAtOrder;
    // 订单项类型：PRODUCT/HOTEL
    @Column(nullable = false, length = 20)
    private String itemType;
    // 下单时的名称快照，防止商品名称后续变更影响历史订单
    @Column(length = 100)
    private String itemNameSnapshot;
    // 下单时的单价快照
    private Double unitPriceSnapshot;
    // 酒店ID（酒店订单使用）
    private Long hotelId;
    // 订房间数（酒店订单使用）
    private Integer roomCount;
    // 入住晚数（酒店订单使用）
    private Integer nightCount;
    // 入住日期（酒店订单使用）
    private LocalDate checkInDate;
    // 退房日期（酒店订单使用）
    private LocalDate checkOutDate;

    //外键：指向 Order
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    //外键：指向 Product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Getters and Setters

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemNameSnapshot() {
        return itemNameSnapshot;
    }

    public void setItemNameSnapshot(String itemNameSnapshot) {
        this.itemNameSnapshot = itemNameSnapshot;
    }

    public Double getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(Double unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
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
}
