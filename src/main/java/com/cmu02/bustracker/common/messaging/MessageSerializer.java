package com.cmu02.bustracker.common.messaging;

import java.util.List;
import java.util.Optional;

/**
 * 메시지 브로커 통신에 사용되는 직렬화/역직렬화 인터페이스.
 * <p>
 * 브로커 전송 포맷(JSON, Protobuf 등)을 구현체에서 결정하며,
 * 비즈니스 로직은 이 인터페이스에만 의존한다.
 * </p>
 */
public interface MessageSerializer {

    /**
     * 객체를 문자열로 직렬화한다.
     *
     * @param object 직렬화할 객체
     * @param <T>    객체 타입
     * @return 직렬화된 문자열, 실패 시 {@link Optional#empty()}
     */
    <T> Optional<String> serialize(T object);

    /**
     * 문자열을 지정한 타입의 객체로 역직렬화한다.
     *
     * @param data  역직렬화할 문자열
     * @param clazz 대상 타입 클래스
     * @param <T>   대상 타입
     * @return 역직렬화된 객체, 실패 시 {@link Optional#empty()}
     */
    <T> Optional<T> deserialize(String data, Class<T> clazz);

    /**
     * 문자열을 제네릭 컬렉션 등 복합 타입으로 역직렬화한다.
     *
     * @param data        역직렬화할 문자열
     * @param elementType 리스트 요소 타입
     * @param <T>         요소 타입
     * @return 역직렬화된 리스트, 실패 시 빈 리스트가 아닌 {@link Optional#empty()}
     */
    <T> Optional<List<T>> deserializeList(String data, Class<T> elementType);
}
