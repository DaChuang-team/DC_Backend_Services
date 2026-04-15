package org.dachuang_team.dc_backend_services.enumeration;

public enum RefundStatus {
    PENDING, // 待处理，用户提交退款申请后进入此状态
    APPROVED, // 经过商家核实同意退款
    AUTO_APPROVED, // 退款申请自动通过(仅限订单未发货或商家超过48小时未处理)
    REJECTED, // 退款申请被商家拒绝，用户可选择再次发起
    CANCELLED, // 退款申请被用户取消，订单继续正常流程
    PENDING_RETURN, // 等待用户退回商品
    RETURNING, // 用户已发出退货，等待商家确认收货
    RETURN_RECEIVED, // 商家已收到退货，等待商家处理退款
    REFUNDED; // 退款已打款，流程结束

    public static boolean isValidStatus(String status) {
        for (RefundStatus s : RefundStatus.values()) {
            if (s.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
