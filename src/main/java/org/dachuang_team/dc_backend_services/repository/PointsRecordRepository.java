package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.PointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Integer> {
    List<PointsRecord> findByUserId(Long userId);

    List<PointsRecord> findByUserIdAndChangeTimeAfterOrderByChangeTimeDesc(Long userId, LocalDateTime changeTime);
}
