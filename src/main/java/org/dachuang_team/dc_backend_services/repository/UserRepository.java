package org.dachuang_team.dc_backend_services.repository;

import org.apache.ibatis.annotations.Param;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserGeneral, Long> {
    UserGeneral findByUserName(@Param("userName") String userName);

    boolean existsByUserPhone(String userPhone);

    boolean existsByUserName(String userName);

    UserGeneral findByUserPhone(String userPhone);

    @Query("SELECT COUNT(u) FROM UserGeneral u WHERE u.createTime >= :startDate")
    long countByCreateTimeAfter(@Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = """
            SELECT DATE_FORMAT(u.create_time, '%Y-%m') as month, COUNT(u.user_id) as count
            FROM user_general u
            WHERE u.create_time >= :startDate
            GROUP BY DATE_FORMAT(u.create_time, '%Y-%m')
            ORDER BY month ASC
            """, nativeQuery = true)
    java.util.List<Object[]> findMonthlyUserStats(@Param("startDate") java.time.LocalDateTime startDate);
}