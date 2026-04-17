package org.dachuang_team.dc_backend_services.repository;

import org.apache.ibatis.annotations.Param;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserGeneral, Long> {
    UserGeneral findByUserName(@Param("userName") String userName);

    boolean existsByUserPhone(String userPhone);

    boolean existsByUserName(String userName);

    UserGeneral findByUserPhone(String userPhone);
}