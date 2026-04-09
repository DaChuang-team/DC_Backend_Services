package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.Order;

public interface PaymentProvider {
    String pay(Order order);
    void refund(Order order);
}

