package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.adapter.in.web.dto.TimeDepositResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.ikigaidigital.common.TimeDepositConstants.LAST_KNOWN_RESPONSE_CACHE;

/** Last successful GET response per request, served (marked stale) when the database is unavailable. */
@Component
public class LastKnownResponseCache {

    private final Cache cache;

    public LastKnownResponseCache(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(LAST_KNOWN_RESPONSE_CACHE);
    }

    public void put(String key, List<TimeDepositResponse> body) {
        cache.put(key, body);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<TimeDepositResponse>> get(String key) {
        return Optional.ofNullable(cache.get(key, List.class));
    }
}


