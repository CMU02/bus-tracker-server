package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.common.messaging.MessageBrokerClient;
import com.cmu02.bustracker.common.messaging.Subscription;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class NatsClient implements MessageBrokerClient {

    private final Connection connection;

    @Override
    public void publish(String topic, String message) {
        connection.publish(topic, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Subscription subscribe(String topic, Consumer<String> handler) {
        Dispatcher dispatcher = connection.createDispatcher(msg -> {
            String body = extractBody(msg);
            try {
                handler.accept(body);
            } catch (Exception e) {
                log.error("NATS 메시지 처리 중 오류 발생 — topic: {}", topic, e);
            }
        });

        dispatcher.subscribe(topic);
        return () -> connection.closeDispatcher(dispatcher);
    }

    @Override
    public Subscription subscribeWithReply(String topic, String queueGroup, BiConsumer<String, ReplyCallback> handler) {
        Dispatcher dispatcher = connection.createDispatcher(msg -> {
            String body = extractBody(msg);
            // reply 콜백: 요청자의 replyTo 주소로 응답 전송
            ReplyCallback replyCallback = response ->
                    connection.publish(msg.getReplyTo(),
                            response.getBytes(StandardCharsets.UTF_8));
            try {
                handler.accept(body, replyCallback);
            } catch (Exception e) {
                log.error("NATS reply 처리 중 오류 발생 — topic: {}", topic, e);
            }
        });

        dispatcher.subscribe(topic, queueGroup);
        return () -> connection.closeDispatcher(dispatcher);
    }

    @Override
    public Optional<String> request(String topic, String payload, Duration timeout) {
        try {
            Message reply = connection.request(
                    topic,
                    payload.getBytes(StandardCharsets.UTF_8),
                    timeout
            );
            if (reply == null || reply.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(new String(reply.getData(), StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("NATS request 인터럽트 — topic: {}", topic);
            return Optional.empty();
        }
    }

    private String extractBody(Message msg) {
        byte[] data = msg.getData();
        return (data != null) ? new String(data, StandardCharsets.UTF_8) : "";
    }
}
