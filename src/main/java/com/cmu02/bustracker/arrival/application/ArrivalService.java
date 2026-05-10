package com.cmu02.bustracker.arrival.application;

import com.cmu02.bustracker.arrival.cache.ArrivalCache;
import com.cmu02.bustracker.arrival.domain.BusArrival;
import com.cmu02.bustracker.common.error.StationNotFoundException;
import com.cmu02.bustracker.common.util.ParsingUtils;
import com.cmu02.bustracker.position.domain.BusType;
import com.cmu02.bustracker.seoul.client.SeoulBusArrivalClient;
import com.cmu02.bustracker.seoul.client.SeoulBusRouteClient;
import com.cmu02.bustracker.seoul.dto.SeoulArrivalItem;
import com.cmu02.bustracker.seoul.dto.SeoulStationItem;
import com.cmu02.bustracker.seoul.parser.SeoulBusJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 정류소 도착 예정 조회 서비스.
 * <ol>
 *   <li>Redis 캐시를 먼저 확인한다.</li>
 *   <li>캐시 미스 시 getStaionByRoute로 ord(순서)를 산출한다.</li>
 *   <li>getArrInfoByRoute를 호출해 도착 정보를 가져온다.</li>
 *   <li>vehId가 null·blank·"0"인 슬롯을 제외한 뒤 캐시에 저장한다.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class ArrivalService {

    private final SeoulBusRouteClient routeClient;
    private final SeoulBusArrivalClient arrivalClient;
    private final SeoulBusJsonParser parser;
    private final ArrivalCache cache;

    public List<BusArrival> getArrivals(String routeId, String stationId) {
        Optional<List<BusArrival>> cached = cache.get(routeId, stationId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 정류소 목록에서 stationId에 해당하는 ord(순서)를 조회한다
        String stationJson = routeClient.getStaionByRoute(routeId);
        List<SeoulStationItem> stations = parser.parseStationList(stationJson);

        SeoulStationItem matched = stations.stream()
                .filter(s -> stationId.equals(s.station()))
                .findFirst()
                .orElseThrow(() -> new StationNotFoundException(stationId));

        String stId = matched.station();
        String ord  = matched.seq();

        // 도착 예정 API 호출
        String arrivalJson = arrivalClient.getArrInfoByRoute(stId, routeId, ord);
        List<SeoulArrivalItem> items = parser.parseArrivalList(arrivalJson);

        // 슬롯 추출 및 vehId 유효성 필터링
        List<BusArrival> arrivals = items.stream()
                .flatMap(item -> extractSlots(item).stream())
                .toList();

        cache.put(routeId, stationId, arrivals);

        return arrivals;
    }

    private List<BusArrival> extractSlots(SeoulArrivalItem item) {
        List<BusArrival> slots = new ArrayList<>();

        if (isValidVehicle(item.vehId1())) {
            slots.add(new BusArrival(
                    1,
                    item.vehId1(),
                    item.plainNo1(),
                    item.arrmsg1(),
                    ParsingUtils.parseIntSafe(item.arrprevstationcnt1()),
                    BusType.fromCode(item.busType1())
            ));
        }

        if (isValidVehicle(item.vehId2())) {
            slots.add(new BusArrival(
                    2,
                    item.vehId2(),
                    item.plainNo2(),
                    item.arrmsg2(),
                    ParsingUtils.parseIntSafe(item.arrprevstationcnt2()),
                    BusType.fromCode(item.busType2())
            ));
        }

        return slots;
    }

    private boolean isValidVehicle(String vehId) {
        return vehId != null && !vehId.isBlank() && !"0".equals(vehId);
    }
}
