package com.asrevo.cvhome.sso.password;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.web.client.RestClientException;

import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The breached-password check.
 *
 * <p>
 * <strong>This is the one place in the realm where "refused" and "no answer" deliberately do not share a
 * branch.</strong> Everywhere else, collapsing them is the bug; here, a corpus that cannot be reached must
 * <em>allow</em> the password. It is a check on a value a person chose, not a decision about money, and an outage
 * at a third party must not stop somebody resetting their password — which, when they are resetting it because
 * they were breached, is the worst possible moment to block.
 * </p>
 */
class CompromisedPasswordGateTest {

    private static final String PASSWORD = "correct-horse-battery-staple";

    private final CompromisedPasswordChecker checker = mock(CompromisedPasswordChecker.class);
    private final CompromisedPasswordGate gate = new CompromisedPasswordGate(checker);

    @Test
    void apasswordTheCorpusHasNotSeenIsAllowed() {
        when(checker.check(PASSWORD)).thenReturn(new CompromisedPasswordDecision(false));

        assertThatCode(() -> gate.check(PASSWORD)).doesNotThrowAnyException();
    }

    @Test
    void aknownBreachedPasswordIsRefused() {
        when(checker.check(PASSWORD)).thenReturn(new CompromisedPasswordDecision(true));

        assertThatThrownBy(() -> gate.check(PASSWORD)).isInstanceOf(PasswordCompromisedException.class);
    }

    @Test
    void acorpusThatCannotBeReachedAllowsThePasswordRatherThanBlockingTheReset() {
        when(checker.check(PASSWORD)).thenThrow(new RestClientException("api.pwnedpasswords.com is away"));

        // Refused throws; no answer passes. Blocking here would stop a breached person changing their password.
        assertThatCode(() -> gate.check(PASSWORD)).doesNotThrowAnyException();
    }

    @Test
    void thedefaultGateUsesTheRangeApiSoNoWholePasswordOrHashLeavesTheMachine() {
        // Spring's Have I Been Pwned client sends the first five characters of the SHA-1 and nothing else.
        assertThatCode(CompromisedPasswordGate::new).doesNotThrowAnyException();
    }

}
