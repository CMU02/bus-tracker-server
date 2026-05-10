package com.cmu02.bustracker.arrival.application;

import com.cmu02.bustracker.arrival.cache.ArrivalCache;
import com.cmu02.bustracker.arrival.domain.BusArrival;
import com.cmu02.bustracker.common.error.StationNotFoundException;
import com.cmu02.bustracker.position.domain.BusType;
import com.cmu02.bustracker.seoul.client.SeoulBusArrivalClient;
import com.cmu02.bustracker.seoul.client.SeoulBusRouteClient;
import com.cmu02.bustracker.seoul.dto.SeoulArrivalItem;
import com.cmu02.bustracker.seoul.dto.SeoulStationItem;
import com.cmu02.bustracker.seoul.parser.SeoulBusJsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ArrivalServiceTest {

    @Mock
    private SeoulBusRouteClient routeClient;
    @Mock
    private SeoulBusArrivalClient arrivalClient;
    @Mock
    private SeoulBusJsonParser parser;
    @Mock
    private ArrivalCache cache;

    private ArrivalService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ArrivalService(routeClient, arrivalClient, parser, cache);
    }

    @Test
    @DisplayName("캐시 히트 시 API 호출 없이 캐시 데이터를 반환한다")
    void returnsCachedArrivalsWithoutApiCall() {
        List<BusArrival> cached = List.of(
                new BusArrival(1, "111111111", "서울70사1111", "2분후[3번째 전]", 3, BusType.GENERAL)
        );
        given(cache.get("100100118", "100000001")).willReturn(Optional.of(cached));

        List<BusArrival> result = service.getArrivals("100100118", "100000001");

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(routeClient, arrivalClient);
    }

    @Test
    @DisplayName("캐시 미스 시 서울 API를 호출하고 결과를 캐시에 저장한다")
    void fetchesFromApiOnCacheMissAndStoresResult() {
        // 정류소 목록 stub
        SeoulStationItem station = new SeoulStationItem(
                "5", null, "100000001", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null
        );
        // 도착 항목 stub - 첫번째 버스만 유효, 두번째 버스 vehId="0"
        SeoulArrivalItem item = new SeoulArrivalItem(
                "100000001", null, "5", "100100118", null,
                "111111111", "서울70사1111", "0", "2분후[3번째 전]", "3", "500",
                "0", null, "0", "운행종료", "0", "0"
        );

        given(cache.get("100100118", "100000001")).willReturn(Optional.empty());
        given(routeClient.getStaionByRoute("100100118")).willReturn("{}");
        given(parser.parseStationList("{}")).willReturn(List.of(station));
        given(arrivalClient.getArrInfoByRoute("100000001", "100100118", "5")).willReturn("{}");
        given(parser.parseArrivalList("{}")).willReturn(List.of(item));

        List<BusArrival> result = service.getArrivals("100100118", "100000001");

        // vehId2="0"은 제외, 첫번째 슬롯만 반환
        assertThat(result).hasSize(1);
        BusArrival first = result.get(0);
        assertThat(first.slot()).isEqualTo(1);
        assertThat(first.vehicleId()).isEqualTo("111111111");
        assertThat(first.licensePlate()).isEqualTo("서울70사1111");
        assertThat(first.arrivalMessage()).isEqualTo("2분후[3번째 전]");
        assertThat(first.stopsAway()).isEqualTo(3);
        assertThat(first.busType()).isEqualTo(BusType.GENERAL);

        verify(cache).put("100100118", "100000001", result);
    }

    @Test
    @DisplayName("정류소 목록에 stationId가 없으면 StationNotFoundException을 던진다")
    void throwsStationNotFoundWhenStationMissing() {
        SeoulStationItem otherStation = new SeoulStationItem(
                "3", null, "999999999", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null
        );

        given(cache.get("100100118", "100000001")).willReturn(Optional.empty());
        given(routeClient.getStaionByRoute("100100118")).willReturn("{}");
        given(parser.parseStationList("{}")).willReturn(List.of(otherStation));

        assertThatThrownBy(() -> service.getArrivals("100100118", "100000001"))
                .isInstanceOf(StationNotFoundException.class)
                .hasMessageContaining("100000001");
    }

    @Test
    @DisplayName("vehId가 null·blank·\"0\"인 슬롯을 모두 제외한다")
    void filtersAllInvalidVehicleSlots() {
        SeoulStationItem station = new SeoulStationItem(
                "5", null, "100000001", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null
        );
        // vehId1=null, vehId2=blank → 둘 다 제외
        SeoulArrivalItem item = new SeoulArrivalItem(
                "100000001", null, "5", "100100118", null,
                null, null, "0", "운행종료", "0", "0",
                "  ", null, "0", "운행종료", "0", "0"
        );

        given(cache.get("100100118", "100000001")).willReturn(Optional.empty());
        given(routeClient.getStaionByRoute("100100118")).willReturn("{}");
        given(parser.parseStationList("{}")).willReturn(List.of(station));
        given(arrivalClient.getArrInfoByRoute("100000001", "100100118", "5")).willReturn("{}");
        given(parser.parseArrivalList("{}")).willReturn(List.of(item));

        List<BusArrival> result = service.getArrivals("100100118", "100000001");

        assertThat(result).isEmpty();
    }
}
