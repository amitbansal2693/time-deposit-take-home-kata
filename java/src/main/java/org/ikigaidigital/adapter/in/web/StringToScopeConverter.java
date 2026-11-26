package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.adapter.in.web.dto.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Locale;

/**
 * Binds the {@code scope} query parameter to the {@link Scope} enum, case-insensitively.
 * A blank value falls back to {@link Scope#DEFAULT}; an unknown value returns a 400 listing the options.
 */
@Component
public class StringToScopeConverter implements Converter<String, Scope> {

    @Override
    public Scope convert(String source) {
        if (source.isBlank()) {
            return Scope.DEFAULT;
        }
        try {
            return Scope.valueOf(source.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown scope '" + source + "', expected one of " + Arrays.toString(Scope.values()));
        }
    }
}

