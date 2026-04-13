package org.dachuang_team.dc_backend_services.domain.PO.ImgPO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.RefundRequest;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_image")
public class RefundImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_request")
    @JsonIgnore
    private RefundRequest refundRequest;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isLinked = false; // 是否已关联退货请求

    @Column(nullable = false)
    private Boolean processed = false; // 是否已被压缩处理

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "refund_no")
    private String refundNo;

    @Column(name = "upload_user_id", updatable = false)
    private Long uploadUserId;

    private LocalDateTime uploadTime;

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Boolean getLinked() {
        return isLinked;
    }
    public void setLinked(Boolean linked) {
        isLinked = linked;
    }
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    public RefundRequest getRefundRequest() {
        return refundRequest;
    }
    public void setRefundRequest(RefundRequest refundRequest) {
        this.refundRequest = refundRequest;
    }
    public String getRefundNo() {
        return refundNo;
    }
    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }
    public Long getUploadUserId() {
        return uploadUserId;
    }
    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }
    public Boolean getProcessed() {
        return processed;
    }
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}