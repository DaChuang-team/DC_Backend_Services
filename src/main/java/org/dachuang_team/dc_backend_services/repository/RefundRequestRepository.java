package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.RefundRequest;
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    Optional<RefundRequest> findByRefundNo(String refundNo);

    boolean existsByOrderNumberAndStatus(String orderNumber, RefundStatus status);

    boolean existsByOrderNumberAndStatusNotIn(String orderNumber, Collection<RefundStatus> statuses);

    Integer countByOrderNumberAndStatusIn(String orderNumber, Collection<RefundStatus> statuses);

    Page<RefundRequest> findBySellerIdAndStatus(Long sellerId, RefundStatus status, Pageable pageable);

    Page<RefundRequest> findBySellerId(Long sellerId, Pageable pageable);

    List<RefundRequest> findByOrderNumberOrderByRequestTimeDesc(String orderNumber);
}
