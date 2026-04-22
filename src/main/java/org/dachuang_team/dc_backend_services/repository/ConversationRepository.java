package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Conversation;
import org.dachuang_team.dc_backend_services.enumeration.ConversationStatus;
import org.dachuang_team.dc_backend_services.enumeration.ConversationType;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Conversation findByInitiatorIdAndInitiatorRoleAndTargetIdAndTargetRole(
            Long initiatorId,
            ConversationUserRole initiatorRole,
            Long targetId,
            ConversationUserRole targetRole
    );

    @Query("SELECT c FROM Conversation c WHERE c.conversationType = :type AND " +
            "((c.initiatorId = :userId AND c.initiatorRole = :role) OR " +
            "(c.targetId = :userId AND c.targetRole = :role))")
    Page<Conversation> findUserConversationsWithType(
            @Param("userId") Long userId,
            @Param("role") ConversationUserRole role,
            @Param("type") ConversationType type,
            Pageable pageable
    );

    // 查询某发起方的客服会话（不限状态，用于判断是否存在历史会话）
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.initiatorId = :initiatorId
          AND c.initiatorRole = :initiatorRole
          AND c.conversationType IN :types
        ORDER BY c.createdAt DESC
        """)
    Optional<Conversation> findLatestCustomerServiceConversation(
            @Param("initiatorId") Long initiatorId,
            @Param("initiatorRole") ConversationUserRole initiatorRole,
            @Param("types") List<ConversationType> types);

    // 查询所有待受理的客服会话（客服登录时拉取历史待受理列表用）
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.status = :status
          AND c.conversationType IN :types
        ORDER BY c.createdAt ASC
        """)
    Page<Conversation> findPendingCustomerServiceConversations(
            @Param("status") ConversationStatus status,
            @Param("types") List<ConversationType> types,
            Pageable pageable);

    List<Conversation> findByInitiatorIdAndInitiatorRoleAndConversationTypeOrderByUpdatedAtDesc(
            Long initiatorId,
            ConversationUserRole initiatorRole,
            ConversationType conversationType
    );

}
