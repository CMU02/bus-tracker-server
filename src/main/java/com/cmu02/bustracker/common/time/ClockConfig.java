package com.cmu02.bustracker.common.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 애플리케이션 전반에서 사용하는 Clock 빈을 제공한다.
 * 테스트에서는 이 빈을 mock으로 대체해 시간을 고정할 수 있다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
