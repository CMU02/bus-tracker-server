package com.cmu02.bustracker;

import com.cmu02.bustracker.config.NatsProperties;
import com.cmu02.bustracker.config.PublicDataPortalProperties;
import com.cmu02.bustracker.config.SseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.cmu02.bustracker.seoul.client")
@EnableConfigurationProperties({SseProperties.class, NatsProperties.class, PublicDataPortalProperties.class})
public class BusTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTrackerApplication.class, args);
    }

}
