package org.dachuang_team.dc_backend_services.enumeration;

public enum OrderEvent {
    PAY,              // 支付（预留）
    CONFIRM,          // 商家确认
    SHIP,             // 商家发货
    SERVE,            // 商家提供服务或线下提供商品（无须发货），直接进入已签收状态
    RECEIVE,          // 买家签收
    COMPLETE,         // 买家确认收货
    CANCEL,           // 取消订单
    FULLY_REFUND,     // 全额退款
    AUTO_CANCEL,      // 自动取消（仅限待支付超过15分钟）（预留）
    AUTO_COMPLETE     // 自动完成（仅限已签收超过7天）（预留）
}
