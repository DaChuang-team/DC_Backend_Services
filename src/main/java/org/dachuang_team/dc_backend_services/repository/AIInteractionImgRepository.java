package org.dachuang_team.dc_backend_services.repository;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.AIInteractionImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIInteractionImgRepository extends JpaRepository<AIInteractionImg, Integer> {

    @Modifying
    @Transactional
    List<AIInteractionImg> findAllByUploadTimeBefore(LocalDateTime threeDaysAgo);

    @Modifying
    @Transactional
    void deleteByImageUrl(String imageUrl);
}
