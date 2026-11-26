package org.ikigaidigital.adapter.in.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeDepositApiPropertiesTest {

    @Test
    void constructor_whenMaxIdempotencyKeyLengthWithinColumnSize_thenAccepted() {
        assertThatCode(() -> properties(1)).doesNotThrowAnyException();
        assertThatCode(() -> properties(100)).doesNotThrowAnyException();
    }

    @Test
    void constructor_whenMaxIdempotencyKeyLengthOutsideColumnSize_thenThrows() {
        assertThatThrownBy(() -> properties(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max-idempotency-key-length");
    }

    private static TimeDepositApiProperties properties(int maxIdempotencyKeyLength) {
        return new TimeDepositApiProperties(100, 1000, Duration.ZERO, maxIdempotencyKeyLength);
    }
}
