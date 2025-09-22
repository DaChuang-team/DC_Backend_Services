package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;
    @Column(nullable = false)
    private double price;
    private int category;
    @Column(name = "origin", length = 30)
    private String origin;
    @Column(name = "image_URL", length = 100)
    private String imageUrl;
    @Column(name = "approved")
    private Boolean approved = false;
    private LocalDateTime publishedAt;

    //外键：指向 User_General（卖家）
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User_General seller;

    @OneToMany(mappedBy = "product")
    private List<Order_Item> orderItems; // 关联的订单项（也就是说谁买了这个商品）

    // Getters and Setters

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public User_General getSeller() {
        return seller;
    }

    public void setSeller(User_General seller) {
        this.seller = seller;
    }

    public List<Order_Item> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<Order_Item> orderItems) {
        this.orderItems = orderItems;
    }
}
