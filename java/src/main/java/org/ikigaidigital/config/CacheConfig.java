package org.ikigaidigital.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.time.Duration;

import static org.ikigaidigital.common.TimeDepositConstants.LAST_KNOWN_RESPONSE_CACHE;
import static org.ikigaidigital.common.TimeDepositConstants.TIME_DEPOSITS_CACHE;

/**
 * In-process Caffeine caches, configured by {@code time-deposit.cache.*} in application.properties.
 * With several instances each has its own cache, so the TTL bounds how stale another instance can
 * be after an update; swap in Redis for a shared cache.
 * <p>
 * Highest precedence puts cache advice outside the transaction advice: a cache hit opens no
 * transaction, and eviction after an update runs only after the update has committed.
 */
@Configuration
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig {

    @Bean
    CacheManager cacheManager(TimeDepositCacheProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache(TIME_DEPOSITS_CACHE, cache(properties.ttl(), properties.maxEntries()));
        manager.registerCustomCache(LAST_KNOWN_RESPONSE_CACHE, cache(properties.lastKnownTtl(), properties.maxEntries()));
        return manager;
    }

    private static Cache<Object, Object> cache(Duration ttl, long maxEntries) {
        return Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(maxEntries).build();
    }
}

