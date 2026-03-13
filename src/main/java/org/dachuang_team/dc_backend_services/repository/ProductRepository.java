package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.Product;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByApprovedTrue(Pageable pageable);

    Page<Product> findByApprovedFalse(Pageable pageable);

    Page<Product> findBySeller(User_General seller, Pageable pageable);
}
