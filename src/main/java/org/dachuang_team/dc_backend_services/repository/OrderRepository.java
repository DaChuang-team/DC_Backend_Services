package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status, Pageable pageable);

    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

    Page<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);

    Order findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE %:orderNumber% AND (o.sellerId = :userId OR o.buyerId = :userId)")
    Page<Order> findByOrderNumberLikeAndUser(@Param("orderNumber") String orderNumber, @Param("userId") Long userId, Pageable pageable);

    Page<Order> findBySellerIdAndBuyerId(Long sellerId, Long buyerId, Pageable pageable);

    Page<Order> findBySellerIdAndBuyerIdAndStatus(Long sellerId, Long buyerId, OrderStatus status, Pageable pageable);

    @Query(
            value = """
        SELECT DISTINCT o
        FROM Order o
        JOIN o.items i
        WHERE (o.sellerId = :userId OR o.buyerId = :userId)
          AND LOWER(i.productName) LIKE LOWER(CONCAT('%', :keyWord, '%'))
        """,
            countQuery = """
        SELECT COUNT(DISTINCT o.id)
        FROM Order o
        JOIN o.items i
        WHERE (o.sellerId = :userId OR o.buyerId = :userId)
          AND LOWER(i.productName) LIKE LOWER(CONCAT('%', :keyWord, '%'))
        """
    )
    Page<Order> findByItemKeywordLikeAndUser(@Param("keyWord") String keyWord,
                                             @Param("userId") Long userId,
                                             Pageable pageable);

    Page<Order> findByStatusAndShippedAtBeforeAndAutoReceivedFalse(
            OrderStatus status, LocalDateTime shippedAt, Pageable pageable);

    Page<Order> findByStatusAndReceivedAtBeforeAndAutoCompletedFalse(
            OrderStatus status, LocalDateTime receivedAt, Pageable pageable);

    Page<Order> findByStatusAndCreatedAtBefore(
            OrderStatus status, LocalDateTime createdAt, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :startDate")
    java.math.BigDecimal sumTotalAmountByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate")
    java.math.BigDecimal sumTotalAmountByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT DATE(o.created_at) as date, COUNT(o.id) as count, COALESCE(SUM(o.total_amount), 0) as total
            FROM orders o
            WHERE o.created_at >= :startDate
            GROUP BY DATE(o.created_at)
            ORDER BY date ASC
            """, nativeQuery = true)
    java.util.List<Object[]> findDailyOrderStats(@Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = """
            SELECT DATE_FORMAT(o.created_at, '%Y-%m') as month, COUNT(o.id) as count, COALESCE(SUM(o.total_amount), 0) as total
            FROM orders o
            WHERE o.created_at >= :startDate
            GROUP BY DATE_FORMAT(o.created_at, '%Y-%m')
            ORDER BY month ASC
            """, nativeQuery = true)
    java.util.List<Object[]> findMonthlyOrderStats(@Param("startDate") java.time.LocalDateTime startDate);

}
