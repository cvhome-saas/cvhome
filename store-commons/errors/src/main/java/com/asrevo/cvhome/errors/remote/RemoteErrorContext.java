package com.asrevo.cvhome.errors.remote;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.FieldError;

/**
 * Everything a failed remote call reported, decoded from the wire and handed to a {@link RemoteExceptionFactory} so it
 * can rebuild the exception the remote service meant.
 *
 * @param code      the remote's {@code code}, e.g. {@code PAYMENT.INITIATE.FAILED}; {@code null} when the response
 *                  carried no problem body at all
 * @param detail    the remote's human-readable detail, or {@code null}
 * @param params    structured context the remote sent; never {@code null}
 * @param fieldErrors field-level failures the remote sent; never {@code null}
 * @param service   logical name of the service that failed, derived from the request URI
 * @param status    HTTP status the remote returned, or {@code 0} when the call produced no response at all
 * @param traceId   the remote's {@code traceId}, so its log line can be found from this side; may be {@code null}
 * @param cause     the transport failure, when there was one; {@code null} for an ordinary error response
 */
public record RemoteErrorContext(String code, String detail, Map<String, Object> params,
        List<FieldError> fieldErrors, String service, int status, String traceId, Throwable cause) {

    public RemoteErrorContext {
        params = params == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(params));
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    /**
     * True when the remote never answered — connection refused, DNS failure, read timeout. A factory usually maps this
     * to its "service unavailable" type rather than to a business condition, since no business decision was reached.
     */
    public boolean isTransportFailure() {
        return status == 0;
    }

}
