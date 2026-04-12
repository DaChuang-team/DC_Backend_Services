package org.dachuang_team.dc_backend_services.domain.VO;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RefundRequestVO {
    private String orderNumber;
    private String refundNo;  // 退款流水号，唯一标识一个退款请求
    private Long sellerId;      // 商家ID
    private Long buyerId;       // 买家ID
    private String refundType;  // ALL_NO_RT(仅退款) / ALL_RT(退货退款) / PARTIAL(部分退款)
    private String returnTrackingNo; // 买家寄回商品的物流单号，PENDING_RETURN状态时由买家填入
    private LocalDateTime returnShippedTime; // 买家寄出商品的时间
    private LocalDateTime returnReceivedTime; // 商家收到退货的时间
    private BigDecimal refundAmount;  // 申请退款金额
    private BigDecimal orderTotalAmount;  // 订单总金额(冗余存储)
    private String reason; // 退款原因
    private RefundStatus status; // 退款申请状态
    private String rejectReason;  // 商家拒绝时的原因，只有在REJECTED状态时该字段有值
    private LocalDateTime requestTime;  // 申请时间
    private LocalDateTime LastHandleTime;  // 商家处理时间

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
    public RefundStatus getStatus() {
        return status;
    }
    public void setStatus(RefundStatus status) {
        this.status = status;
    }
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
    public LocalDateTime getLastHandleTime() {
        return LastHandleTime;
    }
    public void setLastHandleTime(LocalDateTime lastHandleTime) {
        this.LastHandleTime = lastHandleTime;
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
