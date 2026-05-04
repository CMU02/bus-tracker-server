package com.cmu02.bustracker.config;

import com.cmu02.bustracker.common.error.RouteNotFoundException;
import com.cmu02.bustracker.nats.NatsConnectionManager;
import com.cmu02.bustracker.nats.RoutePositionPublisher;
import com.cmu02.bustracker.nats.RoutePositionSubscriber;
import com.cmu02.bustracker.position.application.PollerError;
import com.cmu02.bustracker.position.application.PositionStreamService;
import com.cmu02.bustracker.position.application.RoutePollerFactory;
import com.cmu02.bustracker.position.application.RoutePollerRegistry;
import com.cmu02.bustracker.position.application.RoutePositionPoller;
import com.cmu02.bustracker.seoul.client.SeoulBusPositionClient;
import com.cmu02.bustracker.seoul.client.SeoulBusRouteClient;
import com.cmu02.bustracker.seoul.dto.SeoulStationItem;
import com.cmu02.bustracker.seoul.parser.SeoulBusJsonParser;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * NATS URL이 설정된 경우에만 활성화되는 위치 스트리밍 기능 빈들을 등록한다.
 * bustracker.nats.url 프로퍼티가 없으면 이 Configuration 전체가 비활성화된다.
 * 이 경우 PositionStreamController는 503을 반환한다.
 */
@Configuration
@ConditionalOnProperty(prefix = "bustracker.nats", name = "url")
public class NatsFeaturesConfig {

    @Bean
    RoutePositionPublisher routePositionPublisher(NatsConnectionManager connectionManager,
                                                   ObjectMapper objectMapper) {
        return new RoutePositionPublisher(connectionManager::getConnection, objectMapper);
    }

    @Bean
    RoutePositionSubscriber routePositionSubscriber(NatsConnectionManager connectionManager,
                                                     ObjectMapper objectMapper) {
        return new RoutePositionSubscriber(connectionManager::getConnection, objectMapper);
    }

    @Bean
    RoutePollerFactory routePollerFactory(
            SeoulBusRouteClient routeClient,
            SeoulBusPositionClient positionClient,
            SeoulBusJsonParser parser,
            RoutePositionPublisher publisher,
            ScheduledExecutorService routePollerExecutor,
            Clock clock,
            SseProperties sseProps,
            PublicDataPortalProperties keyProps) {

        return (routeId, errorNotifier) -> buildPoller(
                routeId, errorNotifier,
                routeClient, positionClient, parser, publisher,
                routePollerExecutor, clock, sseProps, keyProps);
    }

    private RoutePositionPoller buildPoller(
            String routeId,
            Consumer<PollerError> errorNotifier,
            SeoulBusRouteClient routeClient,
            SeoulBusPositionClient positionClient,
            SeoulBusJsonParser parser,
            RoutePositionPublisher publisher,
            ScheduledExecutorService routePollerExecutor,
            Clock clock,
            SseProperties sseProps,
            PublicDataPortalProperties keyProps) {

        // Seoul API로 정류장 목록을 조회해 routeId 검증 및 endOrd(lastSeq) 산출
        String json = routeClient.getStaionByRoute(routeId);
        List<SeoulStationItem> stations = parser.parseStationList(json);
        if (stations.isEmpty()) {
            throw new RouteNotFoundException(routeId);
        }
        int lastSeq = stations.stream()
                .mapToInt(s -> Integer.parseInt(s.seq()))
                .max()
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        return new RoutePositionPoller(
                routeId, lastSeq, sseProps.intervalSeconds(),
                positionClient, parser, publisher,
                routePollerExecutor, clock, errorNotifier);
    }

    @Bean
    RoutePollerRegistry routePollerRegistry(RoutePollerFactory factory) {
        return new RoutePollerRegistry(factory);
    }

    @Bean
    PositionStreamService positionStreamService(
            RoutePollerRegistry registry,
            RoutePositionSubscriber subscriber,
            TaskScheduler busTrackerTaskScheduler,
            SseProperties sseProps,
            ObjectMapper objectMapper) {
        return new PositionStreamService(registry, subscriber, busTrackerTaskScheduler, sseProps, objectMapper);
    }
}
