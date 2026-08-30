package org.dachuang_team.dc_backend_services.domain.VO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FavoritesVO {
    private Long productId;
    private String productName;
    private String TbImageUrl;
    private BigDecimal price;
    private Long sellerId;
    private LocalDateTime favoriteAt;
    private Boolean approved;

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

    public String getTbImageUrl() {
        return TbImageUrl;
    }

    public void setTbImageUrl(String tbImageUrl) {
        TbImageUrl = tbImageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
