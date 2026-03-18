package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByClientRequestId(String clientRequestId);
    Optional<Order> findByOrderNo(String orderNo);
    Page<Order> findByUserUserId(Long userId, Pageable pageable);
    Page<Order> findByUserUserIdAndStatus(Long userId, Integer status, Pageable pageable);
}
