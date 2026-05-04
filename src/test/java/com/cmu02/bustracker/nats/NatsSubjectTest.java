package com.cmu02.bustracker.nats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NatsSubjectTest {

    @Test
    @DisplayName("routeSnapshot은 bus.position.route.{routeId}.snapshot 형식을 따른다")
    void routeSnapshotFormat() {
        assertThat(NatsSubject.routeSnapshot("100100025"))
                .isEqualTo("bus.position.route.100100025.snapshot");
    }
}
