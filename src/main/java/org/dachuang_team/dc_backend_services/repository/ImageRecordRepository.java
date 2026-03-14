package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.ImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRecordRepository extends JpaRepository<ImageRecord, Long> {

    Optional<ImageRecord> findByUrl(String url);
}