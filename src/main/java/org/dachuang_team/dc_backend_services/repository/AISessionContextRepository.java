package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.AISessionContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AISessionContextRepository extends JpaRepository<AISessionContext, Integer> {
    AISessionContext findByUserId(Long userId);
}
