package org.dachuang_team.dc_backend_services.domain.PO.OrderPO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "refund_request")
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refund_no", nullable = false, unique = true, length = 32)
    private String refundNo;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long sellerId;      // 商家ID
    @Column(nullable = false)
    private Long buyerId;       // 买家ID

    // 退款类型：ALL_NO_RT(仅退款) / ALL_RT(退货退款) / PARTIAL(部分退款)
    @Column(columnDefinition = "VARCHAR(10)", nullable = false)
    private String refundType;

    @Column(columnDefinition = "DECIMAL(10,2)")
    private BigDecimal refundAmount;  // 申请退款金额

    @Column(columnDefinition = "DECIMAL(10,2)")
    private BigDecimal orderTotalAmount;  // 订单总金额(冗余存储)

    @NotBlank
    private String reason;

    // 退款申请状态，纯退款和退货退款两条路径：
    //
    // 纯退款：  PENDING → APPROVED / REJECTED / CANCELLED
    // 退货退款： PENDING → PENDING_RETURN → RETURNING
    //                   → RETURN_RECEIVED → APPROVED / REJECTED
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'", nullable = false)
    private RefundStatus status;

    // 买家寄回商品的物流单号，PENDING_RETURN状态时由买家填入
    @Column(name = "return_tracking_no", length = 64)
    private String returnTrackingNo;

    // 买家寄出商品的时间
    @Column(name = "return_shipped_time")
    private LocalDateTime returnShippedTime;

    // 商家确认收到退货的时间
    @Column(name = "return_received_time")
    private LocalDateTime returnReceivedTime;

    private String rejectReason;  // 商家拒绝时的原因

    private LocalDateTime requestTime;  // 申请时间
    private LocalDateTime lastHandleTime;  // 商家或买家最后一次处理时间，审批或拒绝时更新

    @OneToMany(mappedBy = "refundRequest", cascade = CascadeType.ALL)
    private List<RefundImg> images;  // 退款凭证图片列表

    public RefundStatus getStatus() {
        return status;
    }
    public void setStatus(RefundStatus status) {
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
    public LocalDateTime getLastHandleTime() {
        return lastHandleTime;
    }
    public void setLastHandleTime(LocalDateTime lastHandleTime) {
        this.lastHandleTime = lastHandleTime;
    }
    public List<RefundImg> getImages() {
        return images;
    }
    public void setImages(List<RefundImg> images) {
        this.images = images;
    }
    public String getRefundNo() {
        return refundNo;
    }
    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }
    public String getReturnTrackingNo() {
        return returnTrackingNo;
    }
    public void setReturnTrackingNo(String returnTrackingNo) {
        this.returnTrackingNo = returnTrackingNo;
    }
    public LocalDateTime getReturnReceivedTime() {
        return returnReceivedTime;
    }
    public void setReturnReceivedTime(LocalDateTime returnReceivedTime) {
        this.returnReceivedTime = returnReceivedTime;
    }
    public LocalDateTime getReturnShippedTime() {
        return returnShippedTime;
    }
    public void setReturnShippedTime(LocalDateTime returnShippedTime) {
        this.returnShippedTime = returnShippedTime;
    }
}
