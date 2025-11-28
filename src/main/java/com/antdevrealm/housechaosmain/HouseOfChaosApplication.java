package com.antdevrealm.housechaosmain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableFeignClients
public class HouseOfChaosApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouseOfChaosApplication.class, args);
    }

}
