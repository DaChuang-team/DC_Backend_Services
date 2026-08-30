package org.dachuang_team.dc_backend_services.domain.PO.ImgPO;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation_image")
public class AccommodationImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 处理后的正式 URL（处理完成前为临时原图 URL）
    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "physical_path", length = 255)
    private String physicalPath;

    // 缩略图 URL（仅首图有值，尺寸为 400×400）
    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    // 是否为首图（sortOrder = 0 时为 true）
    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    // 排列顺序 0~9，共支持最多 10 张
    @Column(name = "sort_order")
    private Integer sortOrder;

    // 图像处理标注：上传时为 false，处理完成后更新为 true
    @Column(name = "processed")
    private Boolean processed = false;

    // 是否已绑定住宿，未绑定时为临时图片
    @Column(name = "is_linked")
    private Boolean isLinked = false;

    @Column(name = "accommodation_id")
    private Long accommodationId;

    @Column(name = "upload_merchant_id")
    private Long uploadMerchantId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getAccommodationId() {
        return accommodationId;
    }
    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }
    public Boolean getLinked() {
        return isLinked;
    }
    public void setLinked(Boolean linked) {
        isLinked = linked;
    }
    public Boolean getProcessed() {
        return processed;
    }
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
    public Integer getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    public Boolean getPrimary() {
        return isPrimary;
    }
    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public String getPhysicalPath() {
        return physicalPath;
    }
    public void setPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUploadMerchantId() {
        return uploadMerchantId;
    }
    public void setUploadMerchantId(Long uploadMerchantId) {
        this.uploadMerchantId = uploadMerchantId;
    }
}
