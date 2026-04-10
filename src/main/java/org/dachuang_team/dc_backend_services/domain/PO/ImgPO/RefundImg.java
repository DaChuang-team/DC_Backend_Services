package org.dachuang_team.dc_backend_services.domain.PO.ImgPO;

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

    @ManyToOne
    @JoinColumn(name = "refund_request_id")
    private RefundRequest refundRequest;

    @NotBlank
    private String imageUrl;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean isLinked;

    private String orderNumber;

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
    public RefundRequest getRefundRequest() {
        return refundRequest;
    }
    public void setRefundRequest(RefundRequest refundRequest) {
        this.refundRequest = refundRequest;
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
}