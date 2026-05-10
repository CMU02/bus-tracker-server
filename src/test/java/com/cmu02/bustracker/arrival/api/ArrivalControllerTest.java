package com.cmu02.bustracker.arrival.api;

import com.cmu02.bustracker.arrival.application.ArrivalService;
import com.cmu02.bustracker.arrival.domain.BusArrival;
import com.cmu02.bustracker.common.error.StationNotFoundException;
import com.cmu02.bustracker.position.domain.BusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.profiles.active=test")
class ArrivalControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @MockitoBean
    ArrivalService arrivalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("정상 요청에 200과 도착 목록을 반환한다")
    void returnsArrivalsForValidRequest() throws Exception {
        List<BusArrival> arrivals = List.of(
                new BusArrival(1, "111111111", "서울70사1111", "2분후[3번째 전]", 3, BusType.GENERAL),
                new BusArrival(2, "222222222", "서울70사2222", "6분후[7번째 전]", 7, BusType.LOW_FLOOR)
        );
        given(arrivalService.getArrivals("100100118", "100000001")).willReturn(arrivals);

        mockMvc.perform(get("/api/v1/routes/100100118/stations/100000001/arrivals")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value("100100118"))
                .andExpect(jsonPath("$.stationId").value("100000001"))
                .andExpect(jsonPath("$.arrivals").isArray())
                .andExpect(jsonPath("$.arrivals.length()").value(2))
                .andExpect(jsonPath("$.arrivals[0].slot").value(1))
                .andExpect(jsonPath("$.arrivals[0].vehicleId").value("111111111"))
                .andExpect(jsonPath("$.arrivals[0].licensePlate").value("서울70사1111"))
                .andExpect(jsonPath("$.arrivals[0].arrivalMessage").value("2분후[3번째 전]"))
                .andExpect(jsonPath("$.arrivals[0].stopsAway").value(3))
                .andExpect(jsonPath("$.arrivals[0].busType").value("GENERAL"))
                .andExpect(jsonPath("$.arrivals[1].slot").value(2))
                .andExpect(jsonPath("$.arrivals[1].busType").value("LOW_FLOOR"));
    }

    @Test
    @DisplayName("도착 버스가 없으면 200과 빈 arrivals를 반환한다")
    void returnsEmptyArrivalsWhenNoBusRunning() throws Exception {
        given(arrivalService.getArrivals("100100118", "100000001")).willReturn(List.of());

        mockMvc.perform(get("/api/v1/routes/100100118/stations/100000001/arrivals")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arrivals").isArray())
                .andExpect(jsonPath("$.arrivals").isEmpty());
    }

    @Test
    @DisplayName("정류소를 찾을 수 없으면 404와 STATION_NOT_FOUND 코드를 반환한다")
    void returns404WhenStationNotFound() throws Exception {
        given(arrivalService.getArrivals("100100118", "UNKNOWN"))
                .willThrow(new StationNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/routes/100100118/stations/UNKNOWN/arrivals"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STATION_NOT_FOUND"));
    }
}
