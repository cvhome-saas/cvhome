package com.asrevo.cvhome.sso.token;

import java.util.Locale;
import java.util.Optional;

/**
 * What an impersonated session may do, chosen per session by the operator.
 *
 * <p>
 * Both modes mint the target's own roles, so the operator sees exactly what the merchant sees. The wire value travels
 * as the {@code act_mode} claim, and {@link #READ} is enforced by every resource server's {@code ReadOnlyActorFilter},
 * which refuses each unsafe method for such a token. The permission tokens could not have done it: they cannot tell
 * a list from a save, and a role that could only read would have shown the operator a page of refusals.
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
