package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserCheckInRepository extends JpaRepository<UserCheckIn, Long> {
    // 检查用户在指定时间段内是否已经签到过
    boolean existsByUserIdAndCheckInTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}