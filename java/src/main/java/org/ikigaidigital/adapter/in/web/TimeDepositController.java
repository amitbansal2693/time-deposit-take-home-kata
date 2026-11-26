package org.ikigaidigital.adapter.in.web;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ikigaidigital.adapter.in.web.dto.Scope;
import org.ikigaidigital.adapter.in.web.dto.TimeDepositResponse;
import org.ikigaidigital.adapter.in.web.dto.TimeDepositResponseScopes;
import org.ikigaidigital.application.port.in.BalanceUpdateResult;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.in.UpdateAllTimeDepositBalancesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.ikigaidigital.common.TimeDepositConstants.API_BASE_PATH;
import static org.ikigaidigital.common.TimeDepositConstants.BALANCES_PATH;
import static org.ikigaidigital.common.TimeDepositConstants.DATA_STALE_HEADER;
import static org.ikigaidigital.common.TimeDepositConstants.IDEMPOTENCY_KEY_HEADER;
import static org.ikigaidigital.common.TimeDepositConstants.IDEMPOTENCY_REPLAYED_HEADER;
import static org.ikigaidigital.common.TimeDepositConstants.NEXT_AFTER_ID_HEADER;
import static org.ikigaidigital.common.TimeDepositConstants.READ_CIRCUIT_BREAKER;
import static org.ikigaidigital.common.TimeDepositConstants.READ_RATE_LIMITER;
import static org.ikigaidigital.common.TimeDepositConstants.UPDATE_RATE_LIMITER;

@RestController
@RequestMapping(API_BASE_PATH)
@Tag(name = "Time Deposits")
public class TimeDepositController {

    private static final Logger log = LoggerFactory.getLogger(TimeDepositController.class);

    private final GetAllTimeDepositsUseCase getTimeDeposits;
    private final UpdateAllTimeDepositBalancesUseCase updateAllBalances;
    private final LastKnownResponseCache lastKnownResponses;
    private final TimeDepositApiProperties properties;
    private final TimeDepositResponseScopes scopes;

    public TimeDepositController(GetAllTimeDepositsUseCase getTimeDeposits,
                                 UpdateAllTimeDepositBalancesUseCase updateAllBalances,
                                 LastKnownResponseCache lastKnownResponses,
                                 TimeDepositApiProperties properties,
                                 TimeDepositResponseScopes scopes) {
        this.getTimeDeposits = getTimeDeposits;
        this.updateAllBalances = updateAllBalances;
        this.lastKnownResponses = lastKnownResponses;
        this.properties = properties;
        this.scopes = scopes;
    }

    /**
     * Without paging parameters every deposit is returned, as the assignment requires.
     * With {@code afterId}/{@code limit} it returns one keyset page; keyset paging stays fast at any
     * depth, unlike offset paging. {@code X-Next-After-Id} is set when another page may exist.
     * <p>
     * Configured in application.properties (overridable per profile / environment variable):
     * <ul>
     *   <li>rate limit: {@code resilience4j.ratelimiter.instances.timeDepositsRead.*}</li>
     *   <li>circuit breaker: {@code resilience4j.circuitbreaker.instances.timeDepositsRead.*}</li>
     *   <li>paging and Cache-Control: {@code time-deposit.api.*}</li>
     *   <li>field scopes: {@code time-deposit.api.response-scopes-file}</li>
     * </ul>
     */
    @GetMapping
    @RateLimiter(name = READ_RATE_LIMITER)
    @CircuitBreaker(name = READ_CIRCUIT_BREAKER, fallbackMethod = "lastKnownResponse")
    @Operation(summary = "Retrieve all time deposits with their withdrawals",
            description = "Optional keyset pagination via afterId/limit and field scope via scope (basic, default, full). "
                    + "If the database is unavailable, the last successful response may be returned with header X-Data-Stale: true.")
    public ResponseEntity<List<Map<String, Object>>> getTimeDeposits(
            @Parameter(description = "Return deposits with id greater than this value")
            @RequestParam(required = false) Integer afterId,
            @Parameter(description = "Page size (capped by time-deposit.api.max-page-size)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Field view to return (case-insensitive). Omit for the full 'default' view; "
                    + "'basic' returns only id, planType and balance; 'full' returns every attribute. "
                    + "Views are defined in dto/time-deposit-response-scopes.xml.")
            @RequestParam(required = false) Scope scope) {
        Scope resolvedScope = scope == null ? Scope.DEFAULT : scope;
        PageParams page = PageParams.of(afterId, limit, properties);
        List<TimeDepositView> views = page.paged()
                ? getTimeDeposits.getTimeDeposits(page.afterId(), page.limit())
                : getTimeDeposits.getAllTimeDeposits();
        List<TimeDepositResponse> body = views.stream().map(TimeDepositResponse::from).toList();
        lastKnownResponses.put(page.cacheKey(), body);

        CacheControl cacheControl = CacheControl.maxAge(properties.cacheMaxAge()).cachePrivate().mustRevalidate();
        return withNextPageHeader(ResponseEntity.ok().cacheControl(cacheControl), page, body)
                .body(scopes.project(body, resolvedScope));
    }

    /**
     * Retrying with the same {@code Idempotency-Key} never applies interest twice; the replay is
     * reported with {@code Idempotency-Replayed: true}. Concurrent updates are serialised by a database lock.
     * <p>
     * Configured in application.properties (overridable per profile / environment variable):
     * <ul>
     *   <li>rate limit: {@code resilience4j.ratelimiter.instances.timeDepositsUpdate.*}</li>
     *   <li>batch size and lock timeout: {@code time-deposit.update-batch-size}, {@code time-deposit.update.lock-timeout}</li>
     *   <li>idempotency key length: {@code time-deposit.api.max-idempotency-key-length}</li>
     * </ul>
     */
    @PutMapping(BALANCES_PATH)
    @RateLimiter(name = UPDATE_RATE_LIMITER)
    @Operation(summary = "Apply one month of interest to every time deposit")
    public ResponseEntity<Void> updateBalances(
            @Parameter(description = "Optional client-generated key that makes retries safe (max length: time-deposit.api.max-idempotency-key-length)")
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey) {
        int maxLength = properties.maxIdempotencyKeyLength();
        if (idempotencyKey != null && (idempotencyKey.isBlank() || idempotencyKey.length() > maxLength)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    IDEMPOTENCY_KEY_HEADER + " must be 1-" + maxLength + " characters");
        }
        BalanceUpdateResult result = updateAllBalances.updateAllBalances(idempotencyKey);
        return ResponseEntity.noContent()
                .header(IDEMPOTENCY_REPLAYED_HEADER, String.valueOf(result == BalanceUpdateResult.ALREADY_APPLIED))
                .build();
    }

    // Circuit-breaker fallbacks: only database failures (and an open circuit) are served from the
    // last known response. Other errors, including rate limiting, propagate unchanged.

    private ResponseEntity<List<Map<String, Object>>> lastKnownResponse(Integer afterId, Integer limit, Scope scope, DataAccessException ex) {
        return staleOrRethrow(afterId, limit, scope, ex);
    }

    private ResponseEntity<List<Map<String, Object>>> lastKnownResponse(Integer afterId, Integer limit, Scope scope, TransactionException ex) {
        return staleOrRethrow(afterId, limit, scope, ex);
    }

    private ResponseEntity<List<Map<String, Object>>> lastKnownResponse(Integer afterId, Integer limit, Scope scope, CallNotPermittedException ex) {
        return staleOrRethrow(afterId, limit, scope, ex);
    }

    private ResponseEntity<List<Map<String, Object>>> staleOrRethrow(Integer afterId, Integer limit, Scope scope, RuntimeException ex) {
        PageParams page = PageParams.of(afterId, limit, properties);
        List<TimeDepositResponse> body = lastKnownResponses.get(page.cacheKey()).orElseThrow(() -> ex);
        log.warn("Serving last known response for '{}': {}", page.cacheKey(), ex.getMessage());
        return withNextPageHeader(ResponseEntity.ok().cacheControl(CacheControl.noStore()), page, body)
                .header(DATA_STALE_HEADER, "true")
                .body(scopes.project(body, scope == null ? Scope.DEFAULT : scope));
    }

    private static ResponseEntity.BodyBuilder withNextPageHeader(ResponseEntity.BodyBuilder builder, PageParams page,
                                                                 List<TimeDepositResponse> body) {
        if (page.paged() && body.size() == page.limit()) {
            builder.header(NEXT_AFTER_ID_HEADER, String.valueOf(body.get(body.size() - 1).id()));
        }
        return builder;
    }

    /** Normalised paging parameters; {@code limit} is clamped to [1, maxPageSize]. */
    record PageParams(boolean paged, int afterId, int limit) {

        static PageParams of(Integer afterId, Integer limit, TimeDepositApiProperties properties) {
            if (afterId == null && limit == null) {
                return new PageParams(false, Integer.MIN_VALUE, 0);
            }
            int size = limit == null ? properties.defaultPageSize() : limit;
            return new PageParams(true,
                    afterId == null ? Integer.MIN_VALUE : afterId,
                    Math.max(1, Math.min(size, properties.maxPageSize())));
        }

        String cacheKey() {
            return paged ? afterId + ":" + limit : "all";
        }
    }
}


