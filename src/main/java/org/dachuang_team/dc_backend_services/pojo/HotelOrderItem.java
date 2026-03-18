package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "hotel_order_item")
public class HotelOrderItem {
    // 酒店订单项，保存酒店预订类订单的快照信息
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    // 下单时的总金额
    @Column(nullable = false)
    private Double priceAtOrder;

    // 下单时的酒店名称快照
    @Column(length = 100)
    private String itemNameSnapshot;

    // 下单时的单价快照
    private Double unitPriceSnapshot;

    // 预订酒店ID
    private Long hotelId;

    // 预订房间数
    private Integer roomCount;

    // 入住晚数
    private Integer nightCount;

    // 入住日期
    private LocalDate checkInDate;

    // 退房日期
    private LocalDate checkOutDate;

    // 所属订单
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
