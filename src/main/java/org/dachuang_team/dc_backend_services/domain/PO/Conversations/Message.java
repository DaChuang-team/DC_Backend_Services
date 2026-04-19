package org.dachuang_team.dc_backend_services.domain.PO.Conversations;

import jakarta.persistence.*;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.dachuang_team.dc_backend_services.enumeration.MsgType;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type", nullable = false)
    private MsgType msgType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private ConversationUserRole senderRole;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content")
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Boolean getRead() {
        return isRead;
    }
    public void setRead(Boolean read) {
        isRead = read;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public ConversationUserRole getSenderRole() {
        return senderRole;
    }
    public void setSenderRole(ConversationUserRole senderRole) {
        this.senderRole = senderRole;
    }
    public MsgType getMsgType() {
        return msgType;
    }
    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }
    public Long getConversationId() {
        return conversationId;
    }
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
