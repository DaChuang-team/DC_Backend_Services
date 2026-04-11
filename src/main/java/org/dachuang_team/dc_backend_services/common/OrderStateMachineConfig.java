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
                .end(OrderStatus.REFUNDED)
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
                .event(OrderEvent.PAY) //待支付经过支付后进入已支付状态
                .and()
                .withExternal()
                .source(OrderStatus.PAID).target(OrderStatus.CONFIRMED)
                .event(OrderEvent.CONFIRM) //已支付经过商家确认后进入商家已确认状态
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.SHIPPED)
                .event(OrderEvent.SHIP) //已确认经过商家发货后进入已发货状态
                .and()
                .withExternal()
                .source(OrderStatus.SHIPPED).target(OrderStatus.RECEIVED)
                .event(OrderEvent.RECEIVE) //已发货经过签收后进入已签收状态
                .and()
                .withExternal()
                .source(OrderStatus.RECEIVED).target(OrderStatus.COMPLETED)
                .event(OrderEvent.COMPLETE) //已签收经过买家确认收货后进入已完成状态
                .and()
                // 退款分支（多个源状态都可申请退款）
                .withExternal()
                .source(OrderStatus.PAID).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND) //已支付可以申请退款
                .and()
                .withExternal()
                .source(OrderStatus.CONFIRMED).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND) //已确认可以申请退款
                .and()
                .withExternal()
                .source(OrderStatus.SHIPPED).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND) //已发货可以申请退款
                .and()
                .withExternal()
                .source(OrderStatus.RECEIVED).target(OrderStatus.REFUND_REQUESTED)
                .event(OrderEvent.REQUEST_REFUND) //已签收可以申请退款
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.REFUNDED)
                .event(OrderEvent.APPROVE_REFUND) //退款申请通过进入已退款状态

                // 商家拒绝退款时根据退款前状态回退到对应状态
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

                // 取消（仅待支付可取消）
                .and()
                .withExternal()
                .source(OrderStatus.PENDING_PAYMENT).target(OrderStatus.CANCELLED)
                .event(OrderEvent.CANCEL)
                .and()
                .withExternal()
                .source(OrderStatus.REFUND_REQUESTED).target(OrderStatus.PAID)
                .event(OrderEvent.CANCEL_REFUND)

                // 用户撤销退款时根据退款前状态回退到对应状态
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
                        context.getMessageHeaders().get("preRefundStatus")));
        ;
    }
}
