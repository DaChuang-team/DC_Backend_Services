package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public class RefundRequestDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderNumber;
    @NotBlank(message = "退款原因不能为空")
    private String reason;
    @NotBlank(message = "退款类型不能为空")
    private String refundType; // ALL_NO_RT(仅退款) / ALL_RT(退货退款) / PARTIAL(部分退款不退货)
    private BigDecimal refundAmount; // 仅在refundType为PARTIAL时必填，且必须为正数且不超过订单总金额(由后端验证)
    private List<Long> imageIds;

    public String getRefundType() {
        return refundType;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public List<Long> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
}
