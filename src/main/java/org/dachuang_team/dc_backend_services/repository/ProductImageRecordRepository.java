package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.ProductImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductImageRecordRepository extends JpaRepository<ProductImageRecord, Long> {

    Optional<ProductImageRecord> findByUrl(String url);
}