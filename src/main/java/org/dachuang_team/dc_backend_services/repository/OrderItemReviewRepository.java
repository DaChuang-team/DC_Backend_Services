package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.OrderItemReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemReviewRepository extends JpaRepository<OrderItemReview, Long> {
    boolean existsByOrderItemId(Long orderItemId);

    Page<OrderItemReview> findByProductId(Long productId, Pageable pageable);

    Page<OrderItemReview> findByBuyerId(Long buyerId, Pageable pageable);

    Page<OrderItemReview> findBySellerId(Long sellerId, Pageable pageable);

}

