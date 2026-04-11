package org.dachuang_team.dc_backend_services.domain.PO.OrderPO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "refund_request")
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long sellerId;      // 商家ID
    @Column(nullable = false)
    private Long buyerId;       // 买家ID

    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    private String refundType;  // ALL / PARTIAL

    @Column(columnDefinition = "DECIMAL(10,2)")
    private BigDecimal refundAmount;  // 申请退款金额

    @Column(columnDefinition = "DECIMAL(10,2)")
    private BigDecimal orderTotalAmount;  // 订单总金额(冗余存储)

    @NotBlank
    private String reason;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'", nullable = false)
    private String status;  // PENDING / APPROVED / REJECTED / REFUNDED / CANCELED

    @Enumerated(EnumType.STRING)
    @Column(name = "pre_refund_status", length = 30)
    private OrderStatus preRefundStatus;

    private String rejectReason;  // 商家拒绝时的原因

    private LocalDateTime requestTime;  // 申请时间
    private LocalDateTime handleTime;  // 商家处理时间

    @OneToMany(mappedBy = "refundRequest", cascade = CascadeType.ALL)
    private List<RefundImg> images;  // 退款凭证图片列表

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getRefundType() {
        return refundType;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getOrderTotalAmount() {
        return orderTotalAmount;
    }

    public void setOrderTotalAmount(BigDecimal orderTotalAmount) {
        this.orderTotalAmount = orderTotalAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public List<RefundImg> getImages() {
        return images;
    }

    public void setImages(List<RefundImg> images) {
        this.images = images;
    }

    public OrderStatus getPreRefundStatus() {
        return preRefundStatus;
    }

    public void setPreRefundStatus(OrderStatus preRefundStatus) {
        this.preRefundStatus = preRefundStatus;
    }
}
