package com.cmu02.bustracker.seoul.parser;

import com.cmu02.bustracker.common.error.SeoulApiException;
import com.cmu02.bustracker.common.error.SeoulApiParseException;
import com.cmu02.bustracker.seoul.dto.SeoulApiResult;
import com.cmu02.bustracker.seoul.dto.SeoulArrivalItem;
import com.cmu02.bustracker.seoul.dto.SeoulStationItem;
import com.cmu02.bustracker.seoul.dto.SeoulVehiclePositionItem;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 서울 공공 API JSON 응답을 Seoul DTO 리스트로 변환한다.
 * Seoul API msgHeader.headerCd가 정상이 아닐 때 SeoulApiException을 던진다.
 * Jackson 역직렬화 자체가 실패한 경우는 SeoulApiParseException으로 묶는다.
 */
@Component
@RequiredArgsConstructor
public class SeoulBusJsonParser {

    private static final TypeReference<SeoulApiResult<SeoulVehiclePositionItem>> VEHICLE_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<SeoulApiResult<SeoulStationItem>> STATION_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<SeoulApiResult<SeoulArrivalItem>> ARRIVAL_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public List<SeoulVehiclePositionItem> parseVehiclePositions(String json) {
        SeoulApiResult<SeoulVehiclePositionItem> result = readValue(json, VEHICLE_TYPE);
        validateHeader(result);
        return result.body() == null ? List.of() : result.body().safeItems();
    }

    public List<SeoulStationItem> parseStationList(String json) {
        SeoulApiResult<SeoulStationItem> result = readValue(json, STATION_TYPE);
        validateHeader(result);
        return result.body() == null ? List.of() : result.body().safeItems();
    }

    public List<SeoulArrivalItem> parseArrivalList(String json) {
        SeoulApiResult<SeoulArrivalItem> result = readValue(json, ARRIVAL_TYPE);
        validateHeader(result);
        return result.body() == null ? List.of() : result.body().safeItems();
    }

    private <T> SeoulApiResult<T> readValue(String json, TypeReference<SeoulApiResult<T>> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new SeoulApiParseException("서울 공공 API JSON 파싱에 실패했습니다.", e);
        }
    }

    private void validateHeader(SeoulApiResult<?> result) {
        if (result.header() == null) {
            throw new SeoulApiException("UNKNOWN", "msgHeader가 누락되었습니다.");
        }
        if (!result.header().isSuccess()) {
            throw new SeoulApiException(result.header().headerCd(), result.header().headerMsg());
        }
    }
}
