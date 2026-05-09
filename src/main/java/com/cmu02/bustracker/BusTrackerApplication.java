package com.cmu02.bustracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.cmu02.bustracker.seoul.client")
public class BusTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTrackerApplication.class, args);
    }

}
