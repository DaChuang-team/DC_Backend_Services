package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ShopBannerImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShopBannerImgRepository extends JpaRepository<ShopBannerImg, Long> {
    ShopBannerImg findByImgUrl(String imgUrl);
}
