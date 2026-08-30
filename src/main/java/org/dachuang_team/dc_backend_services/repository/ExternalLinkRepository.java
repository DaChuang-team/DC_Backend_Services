package org.dachuang_team.dc_backend_services.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.ExternalLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExternalLinkRepository extends JpaRepository<ExternalLink, Long> {
    List<ExternalLink> findByAccommodationIdAndApprovedTrueOrderByToppedDesc(Long accommodationId);
    List<ExternalLink> findByAccommodationIdOrderByToppedDesc(Long accommodationId);

    int countByAccommodationId(Long accommodationId);

    @Modifying
    @Query("UPDATE ExternalLink a SET a.topped = false WHERE a.accommodationId = :accommodationId AND a.topped = true")
    void resetToppedFalseByAccommodationId(@Param("accommodationId") Long accommodationId);

    boolean existsByToppedTrue();
}
