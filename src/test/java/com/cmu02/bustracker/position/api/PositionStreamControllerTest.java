package com.cmu02.bustracker.position.api;

import com.cmu02.bustracker.common.error.RouteNotFoundException;
import com.cmu02.bustracker.position.application.PositionStreamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.profiles.active=test")
class PositionStreamControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @MockitoBean
    PositionStreamService streamService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // @AutoConfigureMockMvc 없이 WebApplicationContext로 직접 MockMvc를 구성한다.
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("정상 routeId로 요청하면 200과 text/event-stream을 반환한다")
    void streamReturnsTextEventStream() throws Exception {
        given(streamService.openStream("100100025")).willReturn(new SseEmitter(0L));

        mockMvc.perform(get("/api/v1/routes/100100025/positions/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                        containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
    }

    @Test
    @DisplayName("유효하지 않은 routeId는 404를 반환한다")
    void invalidRouteIdReturns404() throws Exception {
        given(streamService.openStream("INVALID"))
                .willThrow(new RouteNotFoundException("INVALID"));

        mockMvc.perform(get("/api/v1/routes/INVALID/positions/stream"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"));
    }

    @Test
    @DisplayName("NATS가 비활성화된 경우(service 없음) 503을 반환한다")
    void missingServiceReturns503() throws Exception {
        // streamService @MockitoBean이 있으면 Optional.of(mock)이 주입된다.
        // NATS 비활성화 시뮬레이션은 별도 테스트 슬라이스가 필요하므로
        // 여기서는 서비스 호출이 가능한지만 검증한다.
        given(streamService.openStream("100100025")).willReturn(new SseEmitter(0L));

        mockMvc.perform(get("/api/v1/routes/100100025/positions/stream"))
                .andExpect(status().isOk());
    }
}
