package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.RefundImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundImgRepository extends JpaRepository<RefundImg, Long> {
    List<RefundImg> findAllByUploadTimeBeforeAndIsLinkedFalse(LocalDateTime threeDaysAgo);
}
