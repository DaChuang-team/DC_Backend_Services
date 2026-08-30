package org.dachuang_team.dc_backend_services.repository;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRecordRepository extends JpaRepository<ProductImg, Long> {

    Optional<ProductImg> findByUrl(String url);

    List<ProductImg> findByProductId(Long productId);

    List<ProductImg> findAllByCreatedAtBeforeAndIsLinkedFalse(LocalDateTime time);

    List<ProductImg> findByProductIdOrderBySortOrderAsc(Long productId);

    @Modifying
    @Transactional
    void deleteByUrl(String url);
}