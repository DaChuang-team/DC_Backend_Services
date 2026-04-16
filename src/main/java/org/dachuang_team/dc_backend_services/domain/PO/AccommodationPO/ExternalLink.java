package org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_link")
public class ExternalLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 平台标识：CTRIP / FLIGGY / MEITUAN / CUSTOM 等
    @Column(name = "platform", nullable = false, length = 30)
    private String platform;

    // 商家填写的第三方平台跳转链接
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy; // 创建人商户ID

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 是否审核通过，默认 false
    @Column(name = "approved")
    private Boolean approved = false;

    // 是否置顶，默认 false。置顶链接在展示时优先于非置顶链接显示
    @Column(name = "topped")
    private Boolean topped = false;

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
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Boolean getApproved() {
        return approved;
    }
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    public Boolean getTopped() {
        return topped;
    }
    public void setTopped(Boolean topped) {
        this.topped = topped;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
