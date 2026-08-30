package org.dachuang_team.dc_backend_services.domain.PO.ProductPO;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
public class Favorites {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    private String TbImageUrl;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    private LocalDateTime favoriteAt;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTbImageUrl() {
        return TbImageUrl;
    }

    public void setTbImageUrl(String tbImageUrl) {
        TbImageUrl = tbImageUrl;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public LocalDateTime getFavoriteAt() {
        return favoriteAt;
    }

    public void setFavoriteAt(LocalDateTime favoriteAt) {
        this.favoriteAt = favoriteAt;
    }
}
