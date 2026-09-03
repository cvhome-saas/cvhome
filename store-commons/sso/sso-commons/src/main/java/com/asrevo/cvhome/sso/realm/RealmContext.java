package com.asrevo.cvhome.sso.realm;

import java.util.Optional;
import java.util.function.Supplier;

import com.asrevo.cvhome.commons.domain.RealmId;

/**
 * The realm the current thread is working in.
 *
 * <p>
 * It exists because the realm is resolved at the HTTP edge — from the store host, in cua — but is needed far below
 * it, by Hibernate's {@code CurrentTenantIdentifierResolver}, which is handed no request and no security context.
 * A thread-local is the only channel between those two points.
 * </p>
 *
 * <p>
 * <b>There is deliberately no default.</b> {@link #require()} throws when no realm has been entered, and callers
 * must let it: a tenant resolver that answers "some default realm" when it does not know the answer turns a wiring
 * mistake into a cross-tenant data leak, silently and at read time. Failing the request is the only safe outcome.
 * </p>
 *
 * <p>
 * <b>Background work has no request, so it has no realm.</b> A scheduled job — key rotation, audit retention —
 * must enter each realm explicitly with {@link #runIn}, because Hibernate's tenant filter applies to its queries
 * exactly as it does to a request's. A job that "sweeps every realm" cannot be written as one unfiltered query.
 * </p>
 */
public final class RealmContext {

    private static final ThreadLocal<RealmId> CURRENT = new ThreadLocal<>();

    private static final String NO_REALM = """
            No realm has been entered on this thread. Every code path that touches realm-scoped data must run \
            inside RealmContext.runIn — an HTTP request through the realm-resolving filter, or a background job \
            that enters realms explicitly.""";

    private RealmContext() {
    }

    /**
     * The current realm, or empty when the thread is not working inside one.
     */
    public static Optional<RealmId> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * The current realm, or a failure.
     *
     * @throws IllegalStateException always, when no realm has been entered — see the class javadoc for why this is
     *                               not a recoverable condition with a sensible default
     */
    public static RealmId require() {
        RealmId realm = CURRENT.get();
        if (realm == null) {
            throw new IllegalStateException(NO_REALM);
        }
        return realm;
    }

    /**
     * Runs {@code work} with {@code realm} as the current realm, restoring whatever was current before.
     *
     * <p>
     * Restoring rather than clearing keeps nesting honest: a job that enters realm A and, inside it, briefly enters
     * realm B leaves A current afterwards rather than nothing.
     * </p>
     */
    public static void runIn(RealmId realm, Runnable work) {
        callIn(realm, () -> {
            work.run();
            return null;
        });
    }

    /**
     * {@link #runIn} for work that produces a value.
     */
    public static <T> T callIn(RealmId realm, Supplier<T> work) {
        RealmId previous = CURRENT.get();
        CURRENT.set(realm);
        try {
            return work.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

}
