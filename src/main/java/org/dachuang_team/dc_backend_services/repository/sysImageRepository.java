package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.sysImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface sysImageRepository extends JpaRepository<sysImage, Long> {
    List<sysImage> findByPurpose(String purpose);
}