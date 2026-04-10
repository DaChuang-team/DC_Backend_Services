package org.dachuang_team.dc_backend_services.domain.VO;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RefundRequestVO {
    private String orderNumber;
    private Long sellerId;      // 商家ID
    private Long buyerId;       // 买家ID
    private String refundType;  // ALL / PARTIAL
    private BigDecimal refundAmount;  // 申请退款金额
    private BigDecimal orderTotalAmount;  // 订单总金额(冗余存储)
    private String reason;
    private String status; // PENDING / APPROVED / REJECTED / REFUNDED
    private String rejectReason;  // 商家拒绝时的原因，只有在REJECTED状态时该字段有值
    private LocalDateTime requestTime;  // 申请时间
    private LocalDateTime handleTime;  // 商家处理时间

    private List<RefundImg> images;  // 退款凭证图片列表

    public String getRejectReason() {
        return rejectReason;
    }
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
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
}
