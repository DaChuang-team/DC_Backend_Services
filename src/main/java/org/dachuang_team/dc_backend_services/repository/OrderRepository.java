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

}
