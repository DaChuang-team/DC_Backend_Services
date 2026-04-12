package org.dachuang_team.dc_backend_services.enumeration;

public enum OrderStatus {
    PENDING_PAYMENT,   // 待支付
    PAID,              // 已支付
    CONFIRMED,         // 商家已确认
    SHIPPING,          // 已发货
    RECEIVED,          // 买家已签收
    COMPLETED,         // 已完成
    FULLY_REFUNDED,    // 全部退款完成，订单结束
    CANCELLED          // 已取消
}
