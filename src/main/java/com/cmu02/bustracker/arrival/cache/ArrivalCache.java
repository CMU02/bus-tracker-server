package com.cmu02.bustracker.arrival.cache;

import com.cmu02.bustracker.arrival.domain.BusArrival;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 도착 예정 정보 Redis 캐시.
 * key: arrival:v1:{routeId}:{stationId}, TTL 10초.
 * 캐시 읽기·쓰기 실패는 조용히 무시하고 정상 응답 흐름을 유지한다.
 */
@Component
@RequiredArgsConstructor
public class ArrivalCache {

    private static final String KEY_PREFIX = "arrival:v1:";
    private static final Duration TTL = Duration.ofSeconds(10);
    private static final TypeReference<List<BusArrival>> LIST_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public Optional<List<BusArrival>> get(String routeId, String stationId) {
        try {
            String value = redis.opsForValue().get(key(routeId, stationId));
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, LIST_TYPE));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void put(String routeId, String stationId, List<BusArrival> arrivals) {
        try {
            String value = objectMapper.writeValueAsString(arrivals);
            redis.opsForValue().set(key(routeId, stationId), value, TTL);
        } catch (Exception e) {
            // 캐시 쓰기 실패 시 무시하고 정상 응답을 반환한다
        }
    }

    private String key(String routeId, String stationId) {
        return KEY_PREFIX + routeId + ":" + stationId;
    }
}
