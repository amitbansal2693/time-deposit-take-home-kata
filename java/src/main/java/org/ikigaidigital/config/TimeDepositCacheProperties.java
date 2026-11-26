package org.ikigaidigital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Bound from {@code time-deposit.cache.*} in application.yml.
 *
 * @param ttl          lifetime of cached GET results; evicted on every balance update
 * @param lastKnownTtl how long the last successful response may be served as a stale fallback
 * @param maxEntries   maximum entries per cache
 */
@ConfigurationProperties("time-deposit.cache")
public record TimeDepositCacheProperties(Duration ttl, Duration lastKnownTtl, long maxEntries) {
}


