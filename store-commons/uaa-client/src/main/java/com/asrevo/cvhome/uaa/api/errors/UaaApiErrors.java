package com.asrevo.cvhome.uaa.api.errors;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

/**
 * The uaa admin API's error contract: which wire codes become which exception on a caller's side.
 *
 * <p>
 * Same contract as {@code PaymentApiErrors}, reached without Spring. The uaa SDK speaks plain
 * {@code java.net.http}, so there is no {@code @HttpExchange} proxy to narrow an exception for it and no
 * {@code buildClient} to hand the catalog to — {@code AbstractAdminClient} holds this constant itself and resolves
 * against it in {@code verifyResponse}. The decoding it shares with the Spring clients lives in
 * {@link com.asrevo.cvhome.errors.remote.RemoteFailures}.
 * </p>
 *
 * <p>
 * The mappings rebuild <em>caller-side</em> types. uaa's own {@code UserNotFoundException} describes its database;
 * handing one of those to a caller of this SDK would say the caller queried that database, which it did not — it
 * called uaa. Hence {@link UaaUserNotFoundException} and friends, all naming uaa as the remote.
 * </p>
 *
 * <p>
 * Anything not listed here arrives as {@code UnmappedRemoteFailureException} and is wrapped by the SDK into
 * {@link UaaApiUnavailableException}, still carrying uaa's own code and status. Seeing one in a log is the signal
 * that a code deserves an entry below.
 * </p>
 */
public final class UaaApiErrors {

    /**
     * Codes are listed by their enum rather than as string literals, so renaming one in {@link UaaErrors} cannot
     * silently orphan a mapping here.
     */
    public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
            // Answers uaa gave: definitive, and each one is something the caller can act on.
            .map(UaaErrors.USER_NOT_FOUND, UaaUserNotFoundException::from)
            .map(UaaErrors.CLIENT_NOT_FOUND, UaaClientNotFoundException::from)
            .map(UaaErrors.SUPER_ADMIN_IMMUTABLE, UaaOperationForbiddenException::from)
            // Not a uaa-specific code: uaa lets the unique constraint decide, and the shared advice renders the
            // database's refusal as COMMON.DATA_INTEGRITY_VIOLATION. To a caller it still means "that user exists".
            .map(CommonErrors.DATA_INTEGRITY_VIOLATION, UaaConflictException::from)
            // No answer at all, so no server-side counterpart exists — a client type by necessity.
            .unreachable(UaaApiUnavailableException::from)
            .build();

    private UaaApiErrors() {
    }

}
