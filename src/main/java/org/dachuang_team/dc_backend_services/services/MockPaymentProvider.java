package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentProvider.class);

    @Override
    public String pay(Order order) {
        // 模拟支付处理，直接成功
        log.info("Mock支付成功: 订单ID {} \n支付金额: {} ",
                order.getId(), order.getTotalAmount());
        return "MOCK_PAYMENT_SUCCESS";
    }
    @Override
    public void refund(Order order) {
        // 模拟退款处理，直接成功
        log.info("Mock退款成功: 订单ID {} \n退款金额: {} ",
                order.getId(), order.getTotalAmount());
    }
}
