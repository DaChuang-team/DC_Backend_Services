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
        Long orderId = message.getHeaders().get("orderId", Long.class);
        if (orderId == null) {
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
