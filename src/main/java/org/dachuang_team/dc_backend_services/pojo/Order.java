package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Double totalPrice;
    private Integer status; // 0-待支付, 1-已支付, 2-已发货, 3-已签收, 4-已完成, 5-已取消
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String deliveryAddress;

    // 外键：指向 User_General（买家）
    @ManyToOne
    @JoinColumn(name = "user_id")
    private userGeneral user;

    @OneToMany(mappedBy = "order")
    private List<orderItem> orderItems; // 关联的订单项

    // Getters and Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public userGeneral getUser() {
        return user;
    }

    public void setUser(userGeneral user) {
        this.user = user;
    }

    public List<orderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<orderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
