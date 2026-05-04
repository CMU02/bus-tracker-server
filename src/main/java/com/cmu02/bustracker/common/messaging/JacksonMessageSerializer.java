package com.cmu02.bustracker.common.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JacksonMessageSerializer implements MessageSerializer {

    private final ObjectMapper objectMapper;

    @Override
    public <T> Optional<String> serialize(T object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (JacksonException e) {
            log.error("직렬화 실패 — type: {}", object.getClass().getSimpleName(), e);
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> deserialize(String data, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(data, clazz));
        } catch (JacksonException e) {
            log.error("역직렬화 실패 — type: {}", clazz.getSimpleName(), e);
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<List<T>> deserializeList(String data, Class<T> elementType) {
        try {
            var type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            return Optional.of(objectMapper.readValue(data, type));
        } catch (JacksonException e) {
            log.error("리스트 역직렬화 실패 — elementType: {}", elementType.getSimpleName(), e);
            return Optional.empty();
        }
    }
}
