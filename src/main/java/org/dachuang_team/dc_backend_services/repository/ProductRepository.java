package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.ProductPO.Product;
import org.dachuang_team.dc_backend_services.pojo.UserPO.UserGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByApprovedTrue(Pageable pageable);

    Page<Product> findByApprovedFalse(Pageable pageable);

    Page<Product> findBySeller(UserGeneral seller, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<Product> findByproductId(Long pid);

    long countByApprovedTrue();

    long countByApprovedFalse();

    long countBySeller(UserGeneral seller);

    long countByProductNameContainingIgnoreCase(String keyword);
}
