package com.asrevo.cvhome.sso.token;

import java.util.Locale;
import java.util.Optional;

/**
 * What an impersonated session may do, chosen per session by the operator.
 *
 * <p>
 * The mode is expressed in the token's {@code roles} claim, not enforced by a filter: {@link #READ} mints
 * {@code STORE_MODERATOR} — the platform's existing read-only store role — and {@link #WRITE} mints the target's own
 * roles verbatim. Every {@code hasPermission} check on every service then does the right thing without knowing the
 * word "impersonation". The wire value also travels as {@code act_mode}, for the audit trail and the console's banner.
 * </p>
 */
public enum ImpersonationMode {

    READ, WRITE;

    public static Optional<ImpersonationMode> fromWire(String wire) {
        if (wire == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(wire.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException unknown) {
            return Optional.empty();
        }
    }

    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }

}
