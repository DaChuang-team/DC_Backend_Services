package org.dachuang_team.dc_backend_services.domain.VO;

import java.time.LocalDateTime;

public class ConversationVO {
    private Long conversationId;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private String initiatorRole; // 发起者角色
    private Long initiatorId; // 发起者ID
    private String targetRole; // 目标角色
    private Long targetId; // 目标ID
    private Long entryProductId; // 关联的商品ID（如果有）


    public String getTargetRole() {
        return targetRole;
    }
    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
    public Long getConversationId() {
        return conversationId;
    }
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    public Integer getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }
    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    public String getInitiatorRole() {
        return initiatorRole;
    }
    public void setInitiatorRole(String initiatorRole) {
        this.initiatorRole = initiatorRole;
    }
    public Long getInitiatorId() {
        return initiatorId;
    }
    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }
    public Long getTargetId() {
        return targetId;
    }
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    public Long getEntryProductId() {
        return entryProductId;
    }
    public void setEntryProductId(Long entryProductId) {
        this.entryProductId = entryProductId;
    }
}
