package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.AccommodationImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationImgRepository extends JpaRepository<AccommodationImg, Long>{
    List<AccommodationImg> findByAccommodationId(Long accommodationId);
}
