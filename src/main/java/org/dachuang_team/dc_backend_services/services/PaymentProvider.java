package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Order;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

public interface PaymentProvider {
    String pay(Order order);
    void refund(Order order);
}

