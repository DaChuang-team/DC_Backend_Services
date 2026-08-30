package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByApprovedTrue(Pageable pageable);

    Page<Product> findByApprovedFalse(Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findBySellerIdAndApprovedTrue(Long sellerId, Pageable pageable);

    Optional<Product> findByproductId(Long pid);

    long countByApprovedTrue();

    long countBySellerIdAndApprovedTrue(Long sellerId);

    long countByApprovedFalse();

    long countBySellerId(Long sellerId);

    long countByProductNameContainingIgnoreCase(String keyword);

    // 扣减库存，基于行锁定
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.productId = :productId AND p.stock >= :quantity")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 归还库存：退款或取消订单时直接增补
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.productId = :productId")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 增加销量
    @Modifying
    @Query("UPDATE Product p SET p.sales = COALESCE(p.sales, 0) + :quantity WHERE p.productId = :productId")
    int incrementSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    boolean existsBySellerId(Long sellerId);

    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    java.util.List<Object[]> countByCategory();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.publishedAt >= :startDate")
    long countByPublishedAtAfter(@Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = """
            SELECT DATE_FORMAT(p.published_at, '%Y-%m') as month, COUNT(p.product_id) as count
            FROM product p
            WHERE p.published_at >= :startDate
            GROUP BY DATE_FORMAT(p.published_at, '%Y-%m')
            ORDER BY month ASC
            """, nativeQuery = true)
    java.util.List<Object[]> findMonthlyProductStats(@Param("startDate") java.time.LocalDateTime startDate);
}
