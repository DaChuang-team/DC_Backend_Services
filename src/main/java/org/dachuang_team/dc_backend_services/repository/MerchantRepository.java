package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Merchant findByLoginID(String loginID);

    List<Merchant> findByShopName(String shopName);

    boolean existsByShopName(String shopName);

    boolean existsByLoginID(String loginID);
}
