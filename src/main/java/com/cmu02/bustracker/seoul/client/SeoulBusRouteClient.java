package com.cmu02.bustracker.seoul.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 서울 getStaionByRoute 호출용 Feign client.
 * MVP 범위에서는 routeId 유효성 검증과 endOrd(lastSeq) 산출에 사용한다.
 */
@FeignClient(
        name = "seoulBusRouteClient",
        url = "${bustracker.seoul.route-base-url:http://ws.bus.go.kr/api/rest/busRouteInfo}"
)
public interface SeoulBusRouteClient {

    @GetMapping("/getStaionByRoute")
    String getStaionByRoute(
            @RequestParam("serviceKey") String serviceKey,
            @RequestParam("busRouteId") String busRouteId,
            @RequestParam("resultType") String resultType
    );
}
