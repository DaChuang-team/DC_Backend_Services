package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.UserCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface UserCheckInRepository extends JpaRepository<UserCheckIn, Long> {
    // 检查用户在指定时间段内是否已经签到过
    boolean existsByUserIdAndCheckInTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}