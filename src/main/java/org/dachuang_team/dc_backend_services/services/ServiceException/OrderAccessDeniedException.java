package org.dachuang_team.dc_backend_services.services.ServiceException;

// 无权操作
public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException(String message) {
        super(message);
    }
}
