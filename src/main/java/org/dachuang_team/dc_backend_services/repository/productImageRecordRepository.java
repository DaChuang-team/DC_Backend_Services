package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.productImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface productImageRecordRepository extends JpaRepository<productImageRecord, Long> {

    Optional<productImageRecord> findByUrl(String url);
}