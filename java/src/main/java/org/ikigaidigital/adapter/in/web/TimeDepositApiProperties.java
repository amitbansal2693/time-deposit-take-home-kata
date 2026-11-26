package org.ikigaidigital.adapter.in.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static org.ikigaidigital.common.TimeDepositConstants.IDEMPOTENCY_KEY_COLUMN_LENGTH;

/**
 * Bound from {@code time-deposit.api.*} in application.properties.
 *
 * @param defaultPageSize          page size when only {@code afterId} is given
 * @param maxPageSize              upper bound for the {@code limit} query parameter
 * @param cacheMaxAge              Cache-Control max-age for GET responses; 0 means clients revalidate with the ETag
 * @param maxIdempotencyKeyLength  longest accepted {@code Idempotency-Key}; at most the database column size
 */
@ConfigurationProperties("time-deposit.api")
public record TimeDepositApiProperties(int defaultPageSize, int maxPageSize, Duration cacheMaxAge,
                                       int maxIdempotencyKeyLength) {

    public TimeDepositApiProperties {
        // Fail at startup rather than with a database error on the first long key.
        if (maxIdempotencyKeyLength < 1 || maxIdempotencyKeyLength > IDEMPOTENCY_KEY_COLUMN_LENGTH) {
            throw new IllegalArgumentException("time-deposit.api.max-idempotency-key-length must be between 1 and "
                    + IDEMPOTENCY_KEY_COLUMN_LENGTH + " (size of the idempotencyKey column), was " + maxIdempotencyKeyLength);
        }
    }
}
