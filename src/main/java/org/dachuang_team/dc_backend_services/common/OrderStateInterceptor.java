package org.dachuang_team.dc_backend_services.common;

import org.dachuang_team.dc_backend_services.enumeration.OrderEvent;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Component;

@Component
public class OrderStateInterceptor
        extends StateMachineInterceptorAdapter<OrderStatus, OrderEvent> {

    @Override
    public Message<OrderEvent> preEvent(Message<OrderEvent> message,
                                        StateMachine<OrderStatus, OrderEvent> stateMachine) {
        // 从消息头中获取订单 ID，做前置校验
        String orderId = (String) message.getHeaders().get("orderId");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        return message;
    }

    @Override
    public Exception stateMachineError(StateMachine<OrderStatus, OrderEvent> stateMachine,
                                       Exception exception) {
        // 统一包装异常
        return new RuntimeException("订单状态机处理失败: " + exception.getMessage(), exception);
    }
}
