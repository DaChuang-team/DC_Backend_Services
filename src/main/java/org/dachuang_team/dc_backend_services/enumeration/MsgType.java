package org.dachuang_team.dc_backend_services.enumeration;

public enum MsgType {
    TEXT, // 文本消息
    IMAGE, // 图片消息
    SYS // 系统消息（只能由后端配置，前端不可发送该类型）
}
