package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.tokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userSessionRepository extends JpaRepository<tokenSession, Long> {
    Optional<tokenSession> findByToken(String token);

    void deleteByUserIdAndUserRole(Long id, String role);
}
