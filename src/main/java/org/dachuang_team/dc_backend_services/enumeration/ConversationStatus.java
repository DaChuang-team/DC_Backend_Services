package org.dachuang_team.dc_backend_services.enumeration;

public enum ConversationStatus {
    PENDING, // 待处理
    ACTIVE,  // 活跃中(用户跟商家之间的对话始终是活跃的)
    CLOSED   // 已关闭
}
