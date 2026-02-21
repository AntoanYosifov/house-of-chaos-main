package com.antdevrealm.housechaosmain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableCaching
@EnableScheduling
@EnableFeignClients
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)

@SpringBootApplication
public class HouseOfChaosApplication {
    public static void main(String[] args) {
        SpringApplication.run(HouseOfChaosApplication.class, args);
    }
}

