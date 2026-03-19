package org.dachuang_team.dc_backend_services.repository;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.pojo.ProductPO.ProductImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRecordRepository extends JpaRepository<ProductImageRecord, Long> {

    Optional<ProductImageRecord> findByUrl(String url);

    List<ProductImageRecord> findAllByCreatedAtBeforeAndIsLinkedFalse(LocalDateTime time);

    @Modifying
    @Transactional
    void deleteByUrl(String url);
}