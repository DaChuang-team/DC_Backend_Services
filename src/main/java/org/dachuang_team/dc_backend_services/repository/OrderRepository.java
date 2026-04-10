package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status, Pageable pageable);

    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

    Page<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);

    Order findByOrderNumber(String orderNumber);
}
