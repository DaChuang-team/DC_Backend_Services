package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Conversation;
import org.dachuang_team.dc_backend_services.enumeration.ConversationType;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
