package org.ikigaidigital.adapter.in.web.dto;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeDepositResponseScopesTest {

    private static final TimeDepositResponse RESPONSE = TimeDepositResponse.from(
            new TimeDepositView(new TimeDeposit(1, "basic", 1000.0, 30), List.of()));

    private final TimeDepositResponseScopes scopes =
            new TimeDepositResponseScopes(new ClassPathResource("dto/time-deposit-response-scopes.xml"));

    @Test
    void testProject_whenBasicScope_thenReturnsOnlyBasicAttributes() {
        Map<String, Object> projected = scopes.project(List.of(RESPONSE), Scope.BASIC).get(0);

        assertThat(projected).containsOnlyKeys("id", "planType", "balance");
    }

    @Test
    void testProject_whenDefaultScope_thenIncludesWithdrawals() {
        Map<String, Object> projected = scopes.project(List.of(RESPONSE), Scope.DEFAULT).get(0);

        assertThat(projected).containsOnlyKeys("id", "planType", "balance", "days", "withdrawals");
    }

    @Test
    void testProject_whenFullScope_thenIncludesEveryAttribute() {
        Map<String, Object> projected = scopes.project(List.of(RESPONSE), Scope.FULL).get(0);

        assertThat(projected).containsOnlyKeys("id", "planType", "balance", "days", "withdrawals");
    }

    @Test
    void testProject_whenProjecting_thenKeepsOriginalValueTypes() {
        Map<String, Object> projected = scopes.project(List.of(RESPONSE), Scope.BASIC).get(0);

        assertThat(projected.get("balance")).isEqualTo(1000.0);
        assertThat(projected.get("id")).isEqualTo(1);
    }

    @Test
    void testConstructor_whenScopeEnumConstantMissingFromXml_thenThrows() {
        // Only basic/default defined; the FULL enum constant has no matching <scope>.
        assertThatThrownBy(() -> definition(
                "<scope name=\"basic\"><attribute name=\"id\"/></scope>"
                        + "<scope name=\"default\"><attribute name=\"id\"/></scope>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("full");
    }

    @Test
    void testConstructor_whenAttributeNotAResponseField_thenThrows() {
        assertThatThrownBy(() -> definition(
                "<scope name=\"basic\"><attribute name=\"nope\"/></scope>"
                        + "<scope name=\"default\" extends=\"basic\"/><scope name=\"full\" allAttributes=\"true\"/>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nope");
    }

    @Test
    void testConstructor_whenCyclicInheritance_thenThrows() {
        assertThatThrownBy(() -> definition(
                "<scope name=\"basic\" extends=\"full\"/><scope name=\"default\" extends=\"basic\"/>"
                        + "<scope name=\"full\" extends=\"default\"/>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cyclic");
    }

    private static TimeDepositResponseScopes definition(String scopes) {
        String xml = "<dto name=\"TimeDepositResponse\" defaultScope=\"default\">" + scopes + "</dto>";
        return new TimeDepositResponseScopes(new ByteArrayResource(xml.getBytes()));
    }
}
