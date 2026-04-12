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
                .source(OrderStatus.CONFIRMED).target(OrderStatus.SHIPPED)
                .event(OrderEvent.SHIP) // 已确认 -> 已发货，商家发货
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.SERVE) // 已确认 -> 已收货，服务类、线下类订单直接进入已签收状态
                .and()
                .withExternal()
                .source(OrderStatus.SHIPPED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.RECEIVE) // 已发货 -> 已收货，用户确认收货
                .and()
                .withExternal()
                .source(OrderStatus.RECEIVED).target(OrderStatus.COMPLETED)
                .event(OrderEvent.COMPLETE) // 已收货 -> 已完成，订单流程结束

                // 申请退款
                // 原有四个状态均可申请退款
                .and()
                .withExternal()
                .source(OrderStatus.PAID).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND)
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND)
                .and()
                .withExternal()
                .source(OrderStatus.SHIPPED).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND)
                .and()
                .withExternal()
                .source(OrderStatus.RECEIVED).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND)

                // 同意退款
                // 同意部分退款：回到申请前的状态（主流程继续），由 guard 决定目标
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.PAID)
                .event(OrderEvent.APPROVE_REFUND_PARTIAL)
                .guard(context -> "PAID".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.CONFIRMED)
                .event(OrderEvent.APPROVE_REFUND_PARTIAL)
                .guard(context -> "CONFIRMED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.SHIPPED)
                .event(OrderEvent.APPROVE_REFUND_PARTIAL)
                .guard(context -> "SHIPPED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.APPROVE_REFUND_PARTIAL)
                .guard(context -> "RECEIVED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                // 同意全额退款：进入终态
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.FULLY_REFUNDED)
                .event(OrderEvent.APPROVE_REFUND_FULL)

                // 拒绝退款
                // 根据退款前状态回退到对应状态
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.PAID)
                .event(OrderEvent.REJECT_REFUND)
                .guard(context -> "PAID".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.CONFIRMED)
                .event(OrderEvent.REJECT_REFUND)
                .guard(context -> "CONFIRMED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.SHIPPED)
                .event(OrderEvent.REJECT_REFUND)
                .guard(context -> "SHIPPED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.REJECT_REFUND)
                .guard(context -> "RECEIVED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))

                //  撤销退款
                // 根据退款前状态回退到对应状态
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.PAID)
                .event(OrderEvent.CANCEL_REFUND)
                .guard(context -> "PAID".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.CONFIRMED)
                .event(OrderEvent.CANCEL_REFUND)
                .guard(context -> "CONFIRMED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.SHIPPED)
                .event(OrderEvent.CANCEL_REFUND)
                .guard(context -> "SHIPPED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.CANCEL_REFUND)
                .guard(context -> "RECEIVED".equals(
                        context.getMessageHeaders().get("preRefundStatus")))

                // 取消订单
                // 仅待支付状态可取消
                .and()
                .withExternal()
                .source(OrderStatus.PENDING_PAYMENT).target(OrderStatus.CANCELLED)
                .event(OrderEvent.CANCEL);
    }
}
