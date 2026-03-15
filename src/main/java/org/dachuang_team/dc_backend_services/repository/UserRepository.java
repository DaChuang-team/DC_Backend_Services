package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.UserGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserGeneral, Long> {
    UserGeneral findByUserName(String userName);

    boolean existsByUserPhone(String userPhone);

    boolean existsByUserName(String userName);
}