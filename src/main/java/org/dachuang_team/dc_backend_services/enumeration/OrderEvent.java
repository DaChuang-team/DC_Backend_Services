package org.dachuang_team.dc_backend_services.enumeration;

public enum OrderEvent {
    PAY,              // 支付（预留）
    CONFIRM,          // 商家确认
    SHIP,             // 商家发货
    RECEIVE,          // 买家签收
    COMPLETE,         // 买家确认收货
    REQUEST_REFUND,   // 申请退款
    APPROVE_REFUND_PARTIAL,  // 同意部分退款
    APPROVE_REFUND_FULL,     // 同意全额退款
    REJECT_REFUND,    // 商家拒绝退款
    CANCEL,           // 取消订单
    CANCEL_REFUND,    // 撤销退款（仅限退款申请中）
    AUTO_CANCEL,      // 自动取消（仅限待支付超过15分钟）（预留）
    AUTO_COMPLETE     // 自动完成（仅限已签收超过7天）（预留）
}
