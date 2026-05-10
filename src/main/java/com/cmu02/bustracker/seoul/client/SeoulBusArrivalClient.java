package com.cmu02.bustracker.seoul.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 서울 getArrInfoByRoute 호출용 Feign client.
 * serviceKey와 resultType은 FeignConfig RequestInterceptor가 자동으로 추가한다.
 */
@FeignClient(
        name = "seoulBusArrivalClient",
        url = "http://ws.bus.go.kr/api/rest/arrive"
)
public interface SeoulBusArrivalClient {

    @GetMapping("/getArrInfoByRoute")
    String getArrInfoByRoute(
            @RequestParam("stId") String stId,
            @RequestParam("busRouteId") String busRouteId,
            @RequestParam("ord") String ord
    );
}
