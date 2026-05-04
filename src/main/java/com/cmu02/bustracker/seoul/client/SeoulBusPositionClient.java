package com.cmu02.bustracker.seoul.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 서울 getBusPosByRouteSt 호출용 Feign client.
 * 응답을 raw JSON String으로 받아 SeoulBusJsonParser에서 도메인으로 변환한다.
 */
@FeignClient(
        name = "seoulBusPositionClient",
        url = "${bustracker.seoul.position-base-url:http://ws.bus.go.kr/api/rest/buspos}"
)
public interface SeoulBusPositionClient {

    @GetMapping("/getBusPosByRouteSt")
    String getBusPosByRouteSt(
            @RequestParam("serviceKey") String serviceKey,
            @RequestParam("busRouteId") String busRouteId,
            @RequestParam("startOrd") int startOrd,
            @RequestParam("endOrd") int endOrd,
            @RequestParam("resultType") String resultType
    );
}
