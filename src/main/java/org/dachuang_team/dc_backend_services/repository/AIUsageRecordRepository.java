package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.AIPO.AIUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIUsageRecordRepository extends JpaRepository<AIUsageRecord, Integer>{
}
