package com.asrevo.cvhome.s2s.error;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatusCode;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;

/**
 * Everything that happens to a service-to-service call when it fails, for one client.
 *
 * <p>
 * A remote call fails in exactly two ways, and they are not the same thing to a caller: it produced an <em>error
 * response</em>, which is an answer, or it produced <em>no response at all</em>, which is not. Spring surfaces those
 * through different hooks — {@code defaultStatusHandler} never fires for a refused connection — so both are installed
 * here, together, from one catalog. They used to live apart: a lambda inside {@code WebClientsUtils}, a
 * {@code ProblemDetailErrorHandler} class, and a static helper at the bottom of the same utility class. Reading the
 * failure behaviour of a client meant visiting three of them.
 * </p>
 *
 * <p>
 * The translated exception is checked, and the interfaces Spring gives us to hook a client
 * ({@code ResponseSpec.ErrorHandler}, {@code ClientHttpRequestInterceptor}) may only throw {@link IOException} and
 * unchecked exceptions — so it travels inside {@link UncheckedBaseException}. {@link #declaredOrCarrier} opens that
 * carrier at the proxy boundary; without it, the carrier is all a caller would ever see and
 * {@code catch (PaymentGatewayRejectedException e)} could never match.
 * </p>
 */
public final class S2sErrorHandler {

    private final RemoteErrorCatalog catalog;

    /**
     * @param catalog the called API's error contract, or {@code null} for a client that declares none — in which case
     *                every failure falls back to {@code UnmappedRemoteFailureException}, still carrying whatever the
     *                remote reported
     */
    public S2sErrorHandler(RemoteErrorCatalog catalog) {
        this.catalog = catalog == null ? RemoteErrorCatalog.none() : catalog;
    }

    /**
     * Delivers a remote failure to the caller as the type the invoked method declares.
     *
     * <p>
     * The declared exception types of the method are the authority: if the carried cause is one of them, it is
     * rethrown as itself. Anything undeclared cannot be thrown from that signature, so the carrier flows on to the
     * shared advice, which unwraps it and renders the right status anyway — which is why a client that declares
     * nothing behaves exactly as it did before any of this existed.
     * </p>
     */
    public static Throwable declaredOrCarrier(Method method, Throwable thrown) {
        if (thrown instanceof UncheckedBaseException carrier) {
            for (Class<?> declared : method.getExceptionTypes()) {
                if (declared.isInstance(carrier.getCause())) {
                    return carrier.getCause();
                }
            }
        }
        return thrown;
    }

    /**
     * Installs both failure paths on a blocking client.
     */
    public void apply(RestClient.Builder builder) {
        builder.requestInterceptor((request, body, execution) -> {
            try {
                return execution.execute(request, body);
            } catch (IOException | ResourceAccessException e) {
                // A call that never produced a response never reaches defaultStatusHandler, which is why connection
                // refused and read timeout used to escape as a raw ResourceAccessException.
                throw new UncheckedBaseException(
                        RemoteProblemTranslator.unreachable(catalog, request.getURI(), e));
            }
        });
        builder.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
            URI uri = request.getURI();
            String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            throw new UncheckedBaseException(
                    RemoteProblemTranslator.translate(catalog, uri, response.getStatusCode(), body));
        });
    }

    /**
     * Installs translation on a reactive client.
     *
     * <p>
     * Translation only, and deliberately no typed unwrapping: the failure travels inside a {@code Mono}, where a proxy
     * cannot rethrow it as the method's declared checked type. A reactive caller uses {@code onErrorMap}; the shared
     * advice still renders it correctly at the edge. There is also no transport hook here — a reactive transport
     * failure arrives through the same {@code Mono}, not as a thrown {@link ResourceAccessException}.
     * </p>
     */
    public void apply(WebClient.Builder builder) {
        builder.defaultStatusHandler(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new UncheckedBaseException(RemoteProblemTranslator.translate(catalog,
                        response.request().getURI(), response.statusCode(), body))));
    }

}
