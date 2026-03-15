package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.SysImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysImageRepository extends JpaRepository<SysImage, Long> {
    List<SysImage> findByPurpose(String purpose);
}