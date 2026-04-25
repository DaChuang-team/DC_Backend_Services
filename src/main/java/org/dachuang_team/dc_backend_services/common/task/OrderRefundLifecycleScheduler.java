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

        int autoReceived = orderService.autoReceiveExpiredOrders(now.minusDays(7), BATCH_SIZE);
        int autoCompleted = orderService.autoCompleteExpiredOrders(now.minusDays(7), BATCH_SIZE);
        int autoRefunded = orderService.autoHandlePendingRefundTimeout(now, BATCH_SIZE);

        log.info("订单/退款自动流转完成: autoReceived={}, autoCompleted={}, autoRefunded={}",
                autoReceived, autoCompleted, autoRefunded);
    }
}
