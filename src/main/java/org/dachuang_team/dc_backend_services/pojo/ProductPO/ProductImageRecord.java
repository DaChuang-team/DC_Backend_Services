package org.dachuang_team.dc_backend_services.pojo.ProductPO;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_image_record")
public class ProductImageRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;                    // 处理后的正式URL（或临时原图URL）
    private String physicalPath;
    private LocalDateTime createdAt;
    private Boolean isLinked = false;
    private Long productId;

    private Boolean isPrimary = false;     // 仅第0位为true

    private Integer sortOrder;             // 0~4
    private Boolean processed = false;     // 图像处理标注，在预处理时为false，生成大图时更新为true

    private String thumbnailUrl;           // 缩略图url，仅首图有值（400×400）

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Boolean getLinked() {
        return isLinked;
    }

    public void setLinked(Boolean linked) {
        isLinked = linked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhysicalPath() {
        return physicalPath;
    }

    public void setPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}
