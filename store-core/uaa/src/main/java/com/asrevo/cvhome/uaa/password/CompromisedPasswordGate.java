package com.asrevo.cvhome.uaa.password;

import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;

import lombok.extern.slf4j.Slf4j;

/**
 * The breached-password check, when the realm asks for it.
 *
 * <p>
 * Uses Spring Security's Have I Been Pwned range client: only the first five characters of the SHA-1 leave the
 * machine. A transport failure is logged and the password is <em>allowed</em> — this is a check on a value a person
 * chose, not a decision about a payment, so an outage at the corpus must not block a reset. That is the one place in
 * uaa where "no answer" and "refused" deliberately do not share a branch: refused throws, no answer passes.
 * </p>
 */
@Component
@Slf4j
public class CompromisedPasswordGate {

    private final CompromisedPasswordChecker checker;

    public CompromisedPasswordGate() {
        this(new HaveIBeenPwnedRestApiPasswordChecker());
    }

    CompromisedPasswordGate(CompromisedPasswordChecker checker) {
        this.checker = checker;
    }

    public void check(String raw) throws PasswordCompromisedException {
        boolean compromised;
        try {
            compromised = checker.check(raw).isCompromised();
        } catch (RuntimeException e) {
            log.warn("Breached-password check unavailable; allowing the password: {}", e.getMessage());
            return;
        }
        if (compromised) {
            throw PasswordCompromisedException.of();
        }
    }

}
