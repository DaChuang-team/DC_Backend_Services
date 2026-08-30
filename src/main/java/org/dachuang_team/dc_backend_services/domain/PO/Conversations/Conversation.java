package org.dachuang_team.dc_backend_services.domain.PO.Conversations;

import jakarta.persistence.*;
import org.dachuang_team.dc_backend_services.enumeration.ConversationStatus;
import org.dachuang_team.dc_backend_services.enumeration.ConversationType;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 会话ID

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false)
    private ConversationType conversationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_role", nullable = false)
    private ConversationUserRole initiatorRole; // 发起者角色

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId; // 发起者ID（用户ID、商户ID、管理员ID)

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role")
    private ConversationUserRole targetRole;

    @Column(name = "target_id")
    private Long targetId; // 目标ID（用户ID、商户ID、管理员ID)

    @Column(name = "entry_product_id")
    private Long entryProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConversationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;// 最近一次消息的时间

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public ConversationUserRole getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(ConversationUserRole targetRole) {
        this.targetRole = targetRole;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }

    public ConversationUserRole getInitiatorRole() {
        return initiatorRole;
    }

    public void setInitiatorRole(ConversationUserRole initiatorRole) {
        this.initiatorRole = initiatorRole;
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEntryProductId() {
        return entryProductId;
    }

    public void setEntryProductId(Long entryProductId) {
        this.entryProductId = entryProductId;
    }
}
