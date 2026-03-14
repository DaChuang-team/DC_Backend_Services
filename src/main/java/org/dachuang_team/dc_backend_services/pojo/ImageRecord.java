package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "image_record")
public class ImageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;          // 访问路径
    private String physicalPath; // 本地物理绝对路径
    private LocalDateTime createdAt;
    private Boolean isLinked = false; // 是否已绑定到商品
    private Long productId; // 关联的商品ID

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
}
