package com.cmu02.bustracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 스케줄러 빈 설정.
 * - routePollerExecutor: RoutePositionPoller의 폴링 스레드 풀
 * - busTrackerTaskScheduler: SSE heartbeat 전송용 TaskScheduler
 */
@Configuration
public class SchedulerConfig {

    @Bean
    public ScheduledExecutorService routePollerExecutor() {
        // 데몬 스레드로 설정해 JVM 종료 시 자동으로 정리된다.
        return Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("route-poller-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    @Bean
    public TaskScheduler busTrackerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("sse-heartbeat-");
        scheduler.setDaemon(true);
        return scheduler;
    }
}
