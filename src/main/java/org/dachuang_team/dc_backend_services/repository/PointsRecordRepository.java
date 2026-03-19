package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.UserPO.UserPointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<UserPointsRecord, Integer> {
    List<UserPointsRecord> findByUserId(Long userId);

    List<UserPointsRecord> findByUserIdAndChangeTimeAfterOrderByChangeTimeDesc(Long userId, LocalDateTime changeTime);
}
