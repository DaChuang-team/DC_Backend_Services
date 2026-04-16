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

    // 展示顺序，数值越小越靠前
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    // 是否启用，false 时前端不展示该链接
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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
    public Boolean getActive() {
        return isActive;
    }
    public void setActive(Boolean active) {
        isActive = active;
    }
    public Integer getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
}
