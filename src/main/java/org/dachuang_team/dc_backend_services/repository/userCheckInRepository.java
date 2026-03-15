package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.userCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface userCheckInRepository extends JpaRepository<userCheckIn, Long> {
    // 检查用户在指定时间段内是否已经签到过
    boolean existsByUserIdAndCheckInTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}