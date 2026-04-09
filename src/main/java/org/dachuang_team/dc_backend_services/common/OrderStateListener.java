package org.dachuang_team.dc_backend_services.common;

import org.dachuang_team.dc_backend_services.config.RedisConfig;
import org.dachuang_team.dc_backend_services.enumeration.OrderEvent;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@Component
public class OrderStateListener
        extends StateMachineListenerAdapter<OrderStatus, OrderEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStateListener.class);

    @Override
    public void transition(Transition<OrderStatus, OrderEvent> transition) {
        if (transition.getSource() == null) return;
        log.info("订单状态迁移: {} --[{}]--> {}",
                transition.getSource().getId(),
                transition.getTrigger().getEvent(),
                transition.getTarget().getId());
    }

    @Override
    public void stateMachineError(StateMachine<OrderStatus, OrderEvent> stateMachine,
                                  Exception exception) {
        log.error("状态机异常: {}", exception.getMessage());
    }
}
