package com.antdevrealm.housechaosmain.job;

import com.antdevrealm.housechaosmain.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCleanupScheduler {

    private final OrderService orderService;

    @Autowired
    public OrderCleanupScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanCancelledOrders() {
        int retentionDays = 30;
        int deleted = this.orderService.cleanOldCancelledOrders(30);

        if(deleted > 0) {
            log.info("OrderCleanupScheduler removed {} cancelled orders older than {} days.", deleted, retentionDays);
        }
    }
}
