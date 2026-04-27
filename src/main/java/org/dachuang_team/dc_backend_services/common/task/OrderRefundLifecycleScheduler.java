package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderRefundLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderRefundLifecycleScheduler.class);
    private static final int BATCH_SIZE = 200;

    private final OrderService orderService;

    public OrderRefundLifecycleScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    // 每日 00:30 扫描
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 30 0 * * ?")
    public void runAutoTransition() {
        LocalDateTime now = LocalDateTime.now();

        int autoCancelled = orderService.autoCancelExpiredUnpaidOrders(now.minusMinutes(15), BATCH_SIZE);
        int autoReceived = orderService.autoReceiveExpiredOrders(now.minusDays(7), BATCH_SIZE);
        int autoCompleted = orderService.autoCompleteExpiredOrders(now.minusDays(7), BATCH_SIZE);
        int autoRefunded = orderService.autoHandlePendingRefundTimeout(now, BATCH_SIZE);
        int autoCancelPendingReturn = orderService.autoCancelPendingReturnTimeout(now.minusHours(72), BATCH_SIZE);
        int autoReceiveReturned = orderService.autoReceiveReturnedShipments(now.minusDays(7), BATCH_SIZE);
        int autoApproveReturnReceived = orderService.autoApproveReturnReceivedTimeout(now.minusHours(48), BATCH_SIZE);


        log.info("订单/退款自动流转完成 ，自动取消过期未支付订单: {}, 自动确认收货过期订单: {}, 自动完成过期订单: {}, 自动处理待处理退款超时订单: {}, 自动取消待退货超时订单: {}, 自动确认收货退货包裹: {}, 自动审核退货包裹超时订单: {}",
                autoCancelled, autoReceived, autoCompleted, autoRefunded, autoCancelPendingReturn, autoReceiveReturned, autoApproveReturnReceived);
    }
}
