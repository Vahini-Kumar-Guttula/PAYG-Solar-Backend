package com.sunking.payg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaygSolarBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaygSolarBackendApplication.class, args);
    }
}
