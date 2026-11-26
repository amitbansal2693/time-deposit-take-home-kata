package org.ikigaidigital.adapter.in.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static org.ikigaidigital.common.TimeDepositConstants.CORRELATION_ID_HEADER;
import static org.ikigaidigital.common.TimeDepositConstants.CORRELATION_ID_MDC_KEY;

/**
 * Puts a correlation id (from the request header, or generated) into the logging MDC and the
 * response, and optionally logs one line per request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final int MAX_LENGTH = 64;

    private final boolean requestLoggingEnabled;

    public CorrelationIdFilter(@Value("${time-deposit.logging.request-logging-enabled}") boolean requestLoggingEnabled) {
        this.requestLoggingEnabled = requestLoggingEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request.getHeader(CORRELATION_ID_HEADER));
        long startedAt = System.currentTimeMillis();
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            if (requestLoggingEnabled) {
                log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(),
                        response.getStatus(), System.currentTimeMillis() - startedAt);
            }
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private static String resolveCorrelationId(String header) {
        // Only accept short, log-safe values from clients.
        if (header != null && !header.isBlank() && header.length() <= MAX_LENGTH && header.matches("[A-Za-z0-9._-]+")) {
            return header;
        }
        return UUID.randomUUID().toString();
    }
}


