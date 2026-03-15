package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.userGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface userRepository extends JpaRepository<userGeneral, Long> {
    userGeneral findByUserName(String userName);

    boolean existsByUserPhone(String userPhone);

    boolean existsByUserName(String userName);
}