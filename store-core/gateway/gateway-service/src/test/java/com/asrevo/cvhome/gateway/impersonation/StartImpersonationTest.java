package com.asrevo.cvhome.gateway.impersonation;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.gateway.errors.ImpersonationInvalidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Four required fields; the reason most of all, because a row nobody can review is the failure being designed against. */
class StartImpersonationTest {

    private static final String USER = "u";

    private static final String STORE = "s";

    private static final String READ = "read";

    private static final String WHY = "why";

    @Test
    void trimsAcompleteRequest() throws ImpersonationInvalidException {
        StartImpersonation valid = new StartImpersonation(" u ", STORE, " read", "why ").validated();

        assertThat(valid).isEqualTo(new StartImpersonation(USER, STORE, READ, WHY));
    }

    @Test
    void refusesAmissingReasonNamingTheField() {
        assertThatThrownBy(() -> new StartImpersonation(USER, STORE, READ, "  ").validated())
                .isInstanceOf(ImpersonationInvalidException.class)
                .hasMessageContaining("reason");
        assertThatThrownBy(() -> new StartImpersonation(null, STORE, READ, WHY).validated())
                .isInstanceOf(ImpersonationInvalidException.class)
                .hasMessageContaining("userId");
    }

}
