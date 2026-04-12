package org.dachuang_team.dc_backend_services.enumeration;

public enum RefundStatus {
    PENDING, // 待处理，用户提交退款申请后进入此状态
    APPROVED, // 退款申请商家审核通过，等待用户退货或商家提供退款方式
    AUTO_APPROVED, // 退款申请自动审核通过(未发货订单)
    REJECTED, // 退款申请被商家拒绝，用户可选择再次发起
    CANCELLED, // 退款申请被用户取消，订单继续正常流程
    PENDING_RETURN, // 等待用户退回商品，用户已同意退货但尚未发出
    RETURNING, // 用户已发出退货，等待商家确认收货
    RETURN_RECEIVED, // 商家已收到退货，等待商家处理退款
    REFUNDED // 退款完成，退款申请结束
}
