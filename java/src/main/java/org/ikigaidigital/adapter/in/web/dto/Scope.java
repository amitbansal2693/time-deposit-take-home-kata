package org.ikigaidigital.adapter.in.web.dto;

import java.util.Locale;

/**
 * Field views for {@link TimeDepositResponse}, selectable with {@code ?scope=...} on the GET endpoint.
 * The enum name (lower-cased) matches a {@code <scope>} entry in the response-scopes XML; the two are
 * verified to be in sync at startup. {@link #DEFAULT} is used when the request omits the parameter.
 */
public enum Scope {

    /** Minimal view: id, planType, balance. */
    BASIC,

    /** Full assignment view (used when no scope is given): adds days and withdrawals. */
    DEFAULT,

    /** Every available attribute; grows automatically as the response gains fields. */
    FULL;

    /** Name used in the XML definition file, e.g. {@code DEFAULT -> "default"}. */
    public String definitionName() {
        return name().toLowerCase(Locale.ROOT);
    }
}

