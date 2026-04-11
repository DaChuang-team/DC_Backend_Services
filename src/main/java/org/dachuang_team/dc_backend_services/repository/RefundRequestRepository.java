package org.dachuang_team.dc_backend_services.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByOrderNumber(String orderNumber);

    Optional<RefundRequest> findByRefundNo(String refundNo);

    boolean existsByOrderNumberAndStatus(String orderNumber, String status);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) " +
            "FROM RefundRequest r " +
            "WHERE r.orderNumber = :orderNumber " +
            "AND r.status IN ('PENDING', 'APPROVED', 'REFUNDED')")
    BigDecimal sumRefundAmountByOrderNumber(@Param("orderNumber") String orderNumber);
}
