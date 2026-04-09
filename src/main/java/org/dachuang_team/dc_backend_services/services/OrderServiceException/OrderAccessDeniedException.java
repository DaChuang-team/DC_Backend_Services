package org.dachuang_team.dc_backend_services.services.OrderServiceException;

// 无权操作
public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException(String message) {
        super(message);
    }
}
