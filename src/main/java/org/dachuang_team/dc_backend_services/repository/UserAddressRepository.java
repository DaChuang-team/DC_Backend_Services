package org.dachuang_team.dc_backend_services.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByIsDefaultDesc(Long userId);


    int countByUserId(Long userId);

    void deleteById(Long id);

    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.userId = :userId AND a.isDefault = true")
    void resetDefaultByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("update UserAddress a set a.isDefault = :isDefault where a.id = :id")
    void updateDefaultStatus(@Param("id") Long id, @Param("isDefault") boolean isDefault);

    List<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);

    List<UserAddress> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
