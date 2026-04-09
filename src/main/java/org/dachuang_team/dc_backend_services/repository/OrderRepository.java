package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.pojo.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByBuyerId(String buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(String buyerId, OrderStatus status, Pageable pageable);

    Page<Order> findBySellerId(String sellerId, Pageable pageable);

    Page<Order> findBySellerIdAndStatus(String sellerId, OrderStatus status, Pageable pageable);
}
