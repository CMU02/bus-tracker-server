package com.cmu02.bustracker.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 서울 공공 API 응답 공통 본문.
 * itemList가 누락되거나 null이면 빈 리스트로 처리하기 위해 safeItems()를 사용한다.
 */
public record SeoulApiBody<T>(@JsonProperty("itemList") List<T> itemList) {

    public List<T> safeItems() {
        return itemList == null ? List.of() : itemList;
    }
}
