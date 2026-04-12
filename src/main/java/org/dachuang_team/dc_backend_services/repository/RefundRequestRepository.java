package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.RefundRequest;
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    Optional<RefundRequest> findByRefundNo(String refundNo);

    boolean existsByOrderNumberAndStatus(String orderNumber, RefundStatus status);

}
