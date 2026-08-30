package org.dachuang_team.dc_backend_services.domain.VO;

public class ProductVO {
    private Long productId;
    private String productName;
    private double price; //价格
    private int category; //分类
    private String origin; //地区
    private String TbImageUrl; // 首图缩略图URL
    private String description; //商品描述

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTbImageUrl() {
        return TbImageUrl;
    }

    public void setTbImageUrl(String tbImageUrl) {
        TbImageUrl = tbImageUrl;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
