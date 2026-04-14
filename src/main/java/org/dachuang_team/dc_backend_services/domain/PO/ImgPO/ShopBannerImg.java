package org.dachuang_team.dc_backend_services.domain.PO.ImgPO;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_banner_img")
public class ShopBannerImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "img_Url")
    private String imgUrl;

    @Column(name = "processed")
    private Boolean isProcessed = false;

    @Column(name = "linked")
    private Boolean isLinked = false;

    @Column(name = "upload_merchant_id")
    private Long uploadMerchantId;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getUploadMerchantId() {
        return uploadMerchantId;
    }

    public void setUploadMerchantId(Long uploadMerchantId) {
        this.uploadMerchantId = uploadMerchantId;
    }

    public Boolean getLinked() {
        return isLinked;
    }

    public void setLinked(Boolean linked) {
        isLinked = linked;
    }

    public Boolean getProcessed() {
        return isProcessed;
    }

    public void setProcessed(Boolean processed) {
        isProcessed = processed;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
