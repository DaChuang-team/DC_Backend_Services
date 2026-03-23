package org.dachuang_team.dc_backend_services.pojo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProductDTO {
    @JsonProperty("productName")
    private String productName;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("category")
    private Integer category;

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("description")
    private String description;

    @JsonProperty("imgUrl")
    private String imgUrl;

    @JsonProperty("stock")
    private Integer stock;

    private List<Long> imageIds; // 前端传来的图片ID列表（前端必须按用户想要的顺序传）
    // 这里理解起来可能会有些苦难。比如说用户上传了3张图，前端会得到3个ID，假设分别是[10, 11, 12]，
    // 如果用户想让ID=11的图排在第一位，那么前端就应该传[11, 10, 12]，
    // 后端根据这个顺序来设置ProductImageRecord的sortOrder字段（0、1、2）和isPrimary字段（仅第0位为true）

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public List<Long> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
}
