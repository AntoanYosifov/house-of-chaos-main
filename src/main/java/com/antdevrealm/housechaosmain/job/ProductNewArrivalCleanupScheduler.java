package com.antdevrealm.housechaosmain.job;


import com.antdevrealm.housechaosmain.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class ProductNewArrivalCleanupScheduler {
    private static final int DAYS_AS_NEW = 10;
    private final ProductService productService;

    @Autowired
    public ProductNewArrivalCleanupScheduler(ProductService productService) {
        this.productService = productService;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000L)
    public void cleanCancelledOrders() {
        int updated = this.productService.markOldNewArrivalsAsNotNew(DAYS_AS_NEW);
        if(updated > 0) {
            log.info("ProductNewArrivalScheduler marked {} products as not new (older than {} days).",
                    updated, DAYS_AS_NEW);
        }
    }
}
