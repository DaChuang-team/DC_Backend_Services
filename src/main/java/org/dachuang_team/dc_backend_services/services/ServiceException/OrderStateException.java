package org.dachuang_team.dc_backend_services.services.ServiceException;

// 状态流转不合法
public class OrderStateException extends RuntimeException {
    public OrderStateException(String message) {
        super(message);
    }
}

