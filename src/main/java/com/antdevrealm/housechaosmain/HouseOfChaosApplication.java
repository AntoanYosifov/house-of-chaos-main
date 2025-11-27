package com.antdevrealm.housechaosmain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class HouseOfChaosApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouseOfChaosApplication.class, args);
    }

}
