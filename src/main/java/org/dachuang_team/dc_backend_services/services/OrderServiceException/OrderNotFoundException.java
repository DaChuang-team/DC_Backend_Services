package org.dachuang_team.dc_backend_services.services.OrderServiceException;

// 订单不存在
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
