package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.HotelHomestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelHomestayRepository extends JpaRepository<HotelHomestay, Long> {
}
