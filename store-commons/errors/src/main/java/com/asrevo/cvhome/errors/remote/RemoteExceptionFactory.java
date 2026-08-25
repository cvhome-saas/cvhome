package com.asrevo.cvhome.errors.remote;

import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Rebuilds one remote failure as the exception type its client SDK declares.
 *
 * <p>
 * The return type is deliberately {@link RemoteServiceException} rather than {@code BaseException}: a failure that
 * happened in another service must stay recognisable as one, so it keeps the remote status mapping and can never be
 * confused with a condition this service raised itself.
 * </p>
 */
@FunctionalInterface
public interface RemoteExceptionFactory {

    RemoteServiceException create(RemoteErrorContext context);

}
