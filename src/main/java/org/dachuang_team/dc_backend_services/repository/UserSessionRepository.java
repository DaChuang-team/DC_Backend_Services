package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.TokenSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<TokenSession, Long> {
    Optional<TokenSession> findByToken(String token);

    void deleteByUserIdAndUserRole(Long id, String role);

    Optional<TokenSession> findByUserIdAndUserRole(Long userId, String role);
}
