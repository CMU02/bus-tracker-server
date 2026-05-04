package com.cmu02.bustracker.seoul.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${public-data-portal.key.encode}")
    private String serviceKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("serviceKey", serviceKey);
            requestTemplate.query("resultType", "json");
        };
    }
}
