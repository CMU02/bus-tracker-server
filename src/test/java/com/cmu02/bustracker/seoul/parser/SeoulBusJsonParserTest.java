package com.cmu02.bustracker.seoul.parser;

import com.cmu02.bustracker.common.error.SeoulApiException;
import com.cmu02.bustracker.seoul.dto.SeoulStationItem;
import com.cmu02.bustracker.seoul.dto.SeoulVehiclePositionItem;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeoulBusJsonParserTest {

    private SeoulBusJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new SeoulBusJsonParser(new ObjectMapper());
    }

    @Test
    @DisplayName("정상 위치 응답을 차량 목록으로 파싱한다")
    void parsesSuccessfulVehiclePositions() throws Exception {
        String json = readFixture("seoul/bus-position-success.json");

        List<SeoulVehiclePositionItem> items = parser.parseVehiclePositions(json);

        assertThat(items).hasSize(2);
        SeoulVehiclePositionItem first = items.get(0);
        assertThat(first.vehId()).isEqualTo("123456789");
        assertThat(first.plainNo()).isEqualTo("서울70사1234");
        assertThat(first.sectOrd()).isEqualTo("12");
        assertThat(first.stOrd()).isEqualTo("13");
        assertThat(first.stopFlag()).isEqualTo("0");
        assertThat(first.busType()).isEqualTo("1");
    }

    @Test
    @DisplayName("itemList가 비어 있는 정상 응답은 빈 리스트를 반환한다")
    void returnsEmptyListWhenItemListMissing() throws Exception {
        String json = readFixture("seoul/bus-position-empty.json");

        List<SeoulVehiclePositionItem> items = parser.parseVehiclePositions(json);

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("Seoul API 에러 코드 응답은 SeoulApiException을 던진다")
    void throwsOnSeoulApiErrorCode() throws Exception {
        String json = readFixture("seoul/bus-position-error.json");

        assertThatThrownBy(() -> parser.parseVehiclePositions(json))
                .isInstanceOf(SeoulApiException.class)
                .hasMessageContaining("headerCd=4");
    }

    @Test
    @DisplayName("정상 정류장 목록 응답을 파싱한다")
    void parsesStationList() throws Exception {
        String json = readFixture("seoul/station-list-success.json");

        List<SeoulStationItem> items = parser.parseStationList(json);

        assertThat(items).hasSize(3);
        assertThat(items.get(0).seq()).isEqualTo("1");
        assertThat(items.get(2).seq()).isEqualTo("3");
        assertThat(items.get(2).stationNm()).isEqualTo("도착 정류장");
    }

    private String readFixture(String path) throws Exception {
        var resource = new ClassPathResource(path);
        try (var input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
