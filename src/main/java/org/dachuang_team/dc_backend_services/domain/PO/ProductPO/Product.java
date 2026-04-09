package org.dachuang_team.dc_backend_services.domain.PO.ProductPO;

import jakarta.persistence.*;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserGeneral;

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
    private double price; //价格
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
    @Column(name = "category", nullable = false)
    private int category; //分类
    @Column(name = "origin", length = 30)
    private String origin; //地区
    @Column(name = "TbImage_URL", length = 255)
    private String TbImageUrl; // 首图缩略图URL
    @Column(name = "approved")
    private Boolean approved = false; //审核状态，默认为 false，表示未审核通过
    @Column(name = "published_at")
    private LocalDateTime publishedAt; //发布时间，记录商品被创建的时间
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt; //最后修改时间，记录商品被修改的时间
    @Column(name = "description", length = 500)
    private String description; //商品描述

    //外键：指向 User_General（卖家）
    @ManyToOne
    @JoinColumn(name = "seller")
    private UserGeneral seller;

    // 关联的商品订单项
    @OneToMany(mappedBy = "product")
    private List<ProductOrderItem> OrderItems;

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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTbImageUrl() {
        return TbImageUrl;
    }

    public void setTbImageUrl(String imageUrl) {
        this.TbImageUrl = imageUrl;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public UserGeneral getSeller() {
        return seller;
    }

    public void setSeller(UserGeneral seller) {
        this.seller = seller;
    }

    public List<ProductOrderItem> getOrderItems() {
        return OrderItems;
    }

    public void setOrderItems(List<ProductOrderItem> OrderItems) {
        this.OrderItems = OrderItems;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
