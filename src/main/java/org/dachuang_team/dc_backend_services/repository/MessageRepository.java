package org.dachuang_team.dc_backend_services.repository;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Message;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationId(Long conversationId, Pageable pageable);

    // 获取某个会话中最新的一条消息
    Message findTop1ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    // 将指定消息标记为已读
    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id = :messageId AND m.isRead = false")
    void setIsReadTrueById(@Param("messageId") Long messageId);

    // 将当前会话中，别人发给当前用户的未读消息标记为已读
    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.isRead = false AND (m.senderId != :userId OR m.senderRole != :userRole)")
    void markOtherMessagesAsRead(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("userRole") ConversationUserRole userRole
    );

    // 统计当前会话中，别人发给当前用户的未读消息数量
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.isRead = false AND (m.senderId != :userId OR m.senderRole != :userRole)")
    Integer countUnreadMessagesForCurrentUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("userRole") ConversationUserRole userRole
    );
}
