package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.AIPO.AISessionContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AISessionContextRepository extends JpaRepository<AISessionContext, Integer> {
    AISessionContext findByUserId(Long userId);

    AISessionContext findByUserIdAndSessionId(Long userId, String sessionId);

    void deleteByExpireTimeBefore(LocalDateTime lastInteractionTime);

    int countByExpireTimeBefore(LocalDateTime now);
}

