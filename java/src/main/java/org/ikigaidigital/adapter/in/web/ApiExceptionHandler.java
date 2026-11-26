package org.ikigaidigital.adapter.in.web;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps infrastructure failures to RFC 7807 problem responses. Input validation is out of scope
 * for the assignment; Spring's default handling covers malformed parameters.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RequestNotPermitted.class)
    ProblemDetail rateLimited(RequestNotPermitted ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded, retry later");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ProblemDetail updateInProgress(PessimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A balance update is already in progress");
    }

    @ExceptionHandler({DataAccessException.class, TransactionException.class, CallNotPermittedException.class})
    ProblemDetail serviceUnavailable(RuntimeException ex) {
        log.error("Database unavailable", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");
    }
}


