package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findByLogTypeAndModuleContainingIgnoreCase(String logType, String module, Pageable pageable);

    Page<OperationLog> findByLogTypeAndOperatorContainingIgnoreCase(String logType, String operator, Pageable pageable);

    Page<OperationLog> findByLogTypeAndCreatedAtBetween(String logType, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByLogTypeAndModuleContainingIgnoreCaseAndOperatorContainingIgnoreCase(String logType, String module, String operator, Pageable pageable);

    Page<OperationLog> findByLogTypeAndModuleContainingIgnoreCaseAndCreatedAtBetween(String logType, String module, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByLogTypeAndOperatorContainingIgnoreCaseAndCreatedAtBetween(String logType, String operator, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByLogTypeAndModuleContainingIgnoreCaseAndOperatorContainingIgnoreCaseAndCreatedAtBetween(String logType, String module, String operator, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByLogTypeAndResult(String logType, String result, Pageable pageable);

    Page<OperationLog> findByLogTypeAndResultAndModuleContainingIgnoreCase(String logType, String result, String module, Pageable pageable);

    Page<OperationLog> findByLogTypeAndResultAndCreatedAtBetween(String logType, String result, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByLogTypeAndResultAndModuleContainingIgnoreCaseAndCreatedAtBetween(String logType, String result, String module, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByLogTypeOrderByCreatedAtDesc(String logType, Pageable pageable);

    long deleteByCreatedAtBefore(LocalDateTime before);
}
