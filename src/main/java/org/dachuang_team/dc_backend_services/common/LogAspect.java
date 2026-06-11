package org.dachuang_team.dc_backend_services.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.dachuang_team.dc_backend_services.services.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private LogService logService;

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Around("@annotation(businessLog)")
    public Object handleBusinessLog(ProceedingJoinPoint joinPoint, BusinessLog businessLog) throws Throwable {
        String module = businessLog.module();
        String action = businessLog.action();
        String targetType = businessLog.targetType();
        String messageTemplate = businessLog.message();

        Long operatorId = resolveOperatorId();
        String operator = resolveOperatorName(operatorId);
        String targetId = extractTargetId(joinPoint.getArgs());
        String detail = buildDetail(messageTemplate, joinPoint.getArgs());

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            String resultTargetId = resolveResultTargetId(result);
            if (resultTargetId != null && !resultTargetId.isEmpty()) {
                targetId = resultTargetId;
            }

            String finalDetail = (detail != null && !detail.isEmpty() ? detail : action) + " | 耗时: " + duration + "ms";

            logService.saveBusinessLog(module, action, operator, operatorId, targetType, targetId, finalDetail, "SUCCESS");
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            String finalDetail = (detail != null && !detail.isEmpty() ? detail : action) + " | 耗时: " + duration + "ms | 异常: " + e.getMessage();
            logService.saveBusinessLog(module, action, operator, operatorId, targetType, targetId, finalDetail, "FAIL");
            throw e;
        }
    }

    @Around("@annotation(systemLog)")
    public Object handleSystemLog(ProceedingJoinPoint joinPoint, SystemLog systemLog) throws Throwable {
        String module = systemLog.module();
        String action = systemLog.action();
        String detail = String.format("%s.%s(%s)", joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(), argSummary(joinPoint.getArgs()));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            logService.saveSystemLog(module, action, detail + " | 耗时: " + duration + "ms", "SUCCESS", null);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            logService.saveSystemLog(module, action, detail + " | 耗时: " + duration + "ms", "FAIL", errorMsg);
            throw e;
        }
    }

    private String buildDetail(String template, Object[] args) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        String result = template;
        for (int i = 0; i < args.length; i++) {
            String placeholder = "{" + i + "}";
            if (result.contains(placeholder)) {
                String value = argToString(args[i]);
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    private String argToString(Object arg) {
        if (arg == null) return "null";
        if (arg instanceof String s) return s;
        if (arg instanceof Number) return arg.toString();
        return arg.getClass().getSimpleName();
    }

    private String argSummary(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(argToString(args[i]));
        }
        return sb.toString();
    }

    private Long resolveOperatorId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long) {
                return (Long) auth.getPrincipal();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveOperatorName(Long operatorId) {
        if (operatorId != null) {
            return "用户" + operatorId;
        }
        return "SYSTEM";
    }

    private String extractTargetId(Object[] args) {
        if (args == null || args.length == 0) return "";
        for (Object arg : args) {
            if (arg instanceof String s) {
                return s;
            }
            if (arg instanceof Long || arg instanceof Integer) {
                return arg.toString();
            }
        }
        return "";
    }

    private String resolveResultTargetId(Object result) {
        if (result == null) return null;
        if (result instanceof Order o) {
            return o.getOrderNumber();
        }
        return null;
    }
}
