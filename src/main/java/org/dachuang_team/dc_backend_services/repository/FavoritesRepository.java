package org.dachuang_team.dc_backend_services.repository;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Favorites;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritesRepository extends JpaRepository<Favorites, Long> {
    Page<Favorites> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Favorites f WHERE f.userId = :userId AND f.productId = :productId")
    int deleteByUserIdAndProductId(@Param("userId") Long userId,
                                   @Param("productId") Long productId);
}
