package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.AIPO.AiUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiUsageRecordRepository extends JpaRepository<AiUsageRecord, Integer>{
}
