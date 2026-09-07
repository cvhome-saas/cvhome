package com.asrevo.cvhome.gateway.impersonation;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.gateway.errors.ImpersonationInvalidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Two required fields; the reason most of all, because a row nobody can review is the failure being designed against. */
class StartImpersonationTest {

    private static final String USER = "u";

    private static final String WHY = "why";

    @Test
    void trimsAcompleteRequest() throws ImpersonationInvalidException {
        StartImpersonation valid = new StartImpersonation(" u ", "why ").validated();

        assertThat(valid).isEqualTo(new StartImpersonation(USER, WHY));
    }

    @Test
    void refusesAmissingReasonNamingTheField() {
        assertThatThrownBy(() -> new StartImpersonation(USER, "  ").validated())
                .isInstanceOf(ImpersonationInvalidException.class)
                .hasMessageContaining("reason");
        assertThatThrownBy(() -> new StartImpersonation(null, WHY).validated())
                .isInstanceOf(ImpersonationInvalidException.class)
                .hasMessageContaining("userId");
    }

}
