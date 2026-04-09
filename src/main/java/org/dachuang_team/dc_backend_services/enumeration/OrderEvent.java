package org.dachuang_team.dc_backend_services.enumeration;

public enum OrderEvent {
    PAY,              // 支付（预留）
    CONFIRM,          // 商家确认
    SHIP,             // 商家发货
    RECEIVE,          // 买家签收
    COMPLETE,         // 买家确认收货
    REQUEST_REFUND,   // 申请退款
    APPROVE_REFUND,   // 商家同意退款
    REJECT_REFUND,    // 商家拒绝退款
    CANCEL            // 取消订单
}
