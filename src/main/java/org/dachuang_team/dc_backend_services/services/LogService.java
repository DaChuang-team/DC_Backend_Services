package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.OperationLog;
import org.dachuang_team.dc_backend_services.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    public void saveBusinessLog(String module, String action, String operator, Long operatorId,
                                 String targetType, String targetId, String detail, String result) {
        OperationLog log = new OperationLog();
        log.setLogType("BUSINESS");
        log.setModule(module);
        log.setAction(action);
        log.setOperator(operator);
        log.setOperatorId(operatorId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        operationLogRepository.save(log);
    }

    public void saveSystemLog(String module, String action, String detail, String result, String errorMessage) {
        OperationLog log = new OperationLog();
        log.setLogType("SYSTEM");
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setResult(result);
        log.setErrorMessage(errorMessage);
        log.setCreatedAt(LocalDateTime.now());
        operationLogRepository.save(log);
    }

    public Page<OperationLog> getBusinessLogs(String module, String operator, LocalDateTime startDate,
                                               LocalDateTime endDate, Pageable pageable) {
        String logType = "BUSINESS";
        boolean hasModule = module != null && !module.trim().isEmpty();
        boolean hasOperator = operator != null && !operator.trim().isEmpty();
        boolean hasDateRange = startDate != null && endDate != null;

        if (hasModule && hasOperator && hasDateRange) {
            return operationLogRepository.findByLogTypeAndModuleContainingIgnoreCaseAndOperatorContainingIgnoreCaseAndCreatedAtBetween(
                    logType, module, operator, startDate, endDate, pageable);
        } else if (hasModule && hasOperator) {
            return operationLogRepository.findByLogTypeAndModuleContainingIgnoreCaseAndOperatorContainingIgnoreCase(
                    logType, module, operator, pageable);
        } else if (hasModule && hasDateRange) {
            return operationLogRepository.findByLogTypeAndModuleContainingIgnoreCaseAndCreatedAtBetween(
                    logType, module, startDate, endDate, pageable);
        } else if (hasOperator && hasDateRange) {
            return operationLogRepository.findByLogTypeAndOperatorContainingIgnoreCaseAndCreatedAtBetween(
                    logType, operator, startDate, endDate, pageable);
        } else if (hasModule) {
            return operationLogRepository.findByLogTypeAndModuleContainingIgnoreCase(logType, module, pageable);
        } else if (hasOperator) {
            return operationLogRepository.findByLogTypeAndOperatorContainingIgnoreCase(logType, operator, pageable);
        } else if (hasDateRange) {
            return operationLogRepository.findByLogTypeAndCreatedAtBetween(logType, startDate, endDate, pageable);
        } else {
            return operationLogRepository.findByLogTypeOrderByCreatedAtDesc(logType, pageable);
        }
    }

    public Page<OperationLog> getSystemLogs(String module, String result, LocalDateTime startDate,
                                             LocalDateTime endDate, Pageable pageable) {
        String logType = "SYSTEM";
        boolean hasModule = module != null && !module.trim().isEmpty();
        boolean hasResult = result != null && !result.trim().isEmpty();
        boolean hasDateRange = startDate != null && endDate != null;

        if (hasModule && hasResult && hasDateRange) {
            return operationLogRepository.findByLogTypeAndResultAndModuleContainingIgnoreCaseAndCreatedAtBetween(
                    logType, result, module, startDate, endDate, pageable);
        } else if (hasModule && hasResult) {
            return operationLogRepository.findByLogTypeAndResultAndModuleContainingIgnoreCase(
                    logType, result, module, pageable);
        } else if (hasModule && hasDateRange) {
            return operationLogRepository.findByLogTypeAndModuleContainingIgnoreCaseAndCreatedAtBetween(
                    logType, module, startDate, endDate, pageable);
        } else if (hasResult && hasDateRange) {
            return operationLogRepository.findByLogTypeAndResultAndCreatedAtBetween(
                    logType, result, startDate, endDate, pageable);
        } else if (hasModule) {
            return operationLogRepository.findByLogTypeAndModuleContainingIgnoreCase(logType, module, pageable);
        } else if (hasResult) {
            return operationLogRepository.findByLogTypeAndResult(logType, result, pageable);
        } else if (hasDateRange) {
            return operationLogRepository.findByLogTypeAndCreatedAtBetween(logType, startDate, endDate, pageable);
        } else {
            return operationLogRepository.findByLogTypeOrderByCreatedAtDesc(logType, pageable);
        }
    }
}
