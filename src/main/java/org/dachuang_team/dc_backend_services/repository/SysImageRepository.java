package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.SysImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysImageRepository extends JpaRepository<SysImg, Long> {
    List<SysImg> findByPurpose(String purpose);
    SysImg findByImageId(Long imageId);
}