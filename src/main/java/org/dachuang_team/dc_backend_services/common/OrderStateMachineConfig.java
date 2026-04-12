package org.dachuang_team.dc_backend_services.common;

import org.dachuang_team.dc_backend_services.enumeration.OrderEvent;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class OrderStateMachineConfig
        extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states)
            throws Exception {
        states.withStates()
                .initial(OrderStatus.PENDING_PAYMENT)
                .end(OrderStatus.COMPLETED)
                .end(OrderStatus.FULLY_REFUNDED)
                .end(OrderStatus.CANCELLED)
                .states(EnumSet.allOf(OrderStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions)
            throws Exception {
        transitions
                // 主流程
                .withExternal()
                .source(OrderStatus.PENDING_PAYMENT).target(OrderStatus.PAID)
                .event(OrderEvent.PAY) // 待支付 -> 已支付，用户完成支付
                .and()
                .withExternal()
                .source(OrderStatus.PAID).target(OrderStatus.CONFIRMED)
                .event(OrderEvent.CONFIRM) // 已支付 -> 已确认，商家确认订单
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.SHIPPING)
                .event(OrderEvent.SHIP) // 已确认 -> 已发货，商家发货
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.SERVE) // 已确认 -> 已收货，服务类、线下类订单直接进入已签收状态
                .and()
                .withExternal()
                .source(OrderStatus.SHIPPING).target(OrderStatus.RECEIVED)
                .event(OrderEvent.RECEIVE) // 已发货 -> 已收货，用户确认收货
                .and()
                .withExternal()
                .source(OrderStatus.RECEIVED).target(OrderStatus.COMPLETED)
                .event(OrderEvent.COMPLETE) // 已收货 -> 已完成，订单流程结束

                // 退款流程,任何已支付状态都可以发起全额退款，订单流程结束，退款申请由退款服务处理，不阻塞订单流程
                .and()
                .withExternal()
                .source(OrderStatus.PAID).target(OrderStatus.FULLY_REFUNDED)
                .event(OrderEvent.FULLY_REFUND) // 已支付 -> 全部退款完成，订单流程结束
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.FULLY_REFUNDED)
                .event(OrderEvent.FULLY_REFUND) // 已确认 -> 全部退款完成，订单流程结束
                .and()
                .withExternal()
                .source(OrderStatus.SHIPPING).target(OrderStatus.FULLY_REFUNDED)
                .event(OrderEvent.FULLY_REFUND) // 已发货 -> 全部退款完成，订单流程结束
                .and()
                .withExternal()
                .source(OrderStatus.RECEIVED).target(OrderStatus.FULLY_REFUNDED)
                .event(OrderEvent.FULLY_REFUND) // 已收货 -> 全部退款完成，订单流程结束

                // 取消订单
                // 仅待支付状态可取消
                .and()
                .withExternal()
                .source(OrderStatus.PENDING_PAYMENT).target(OrderStatus.CANCELLED)
                .event(OrderEvent.CANCEL);
    }
}
