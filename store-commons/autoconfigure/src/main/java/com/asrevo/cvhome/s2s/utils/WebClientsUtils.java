package com.asrevo.cvhome.s2s.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.s2s.error.S2sErrorHandler;

/**
 * Builds the typed HTTP clients a service uses to call its peers.
 *
 * <p>
 * <strong>Every client is built from a {@code clone()} of the incoming builder, and that is load-bearing.</strong>
 * The builders handed in are shared Spring beans, and both {@code RestClient.Builder} and {@code WebClient.Builder}
 * mutate in place — {@code baseUrl(...)} and each registered interceptor accumulate on the instance. Building
 * straight from the shared builder therefore leaves every client carrying the interceptors of every client built
 * before it, and since the earliest-registered interceptor wraps the call, it is the <em>first</em> API's
 * {@code RemoteErrorCatalog} that translates a transport failure for all of them.
 * </p>
 *
 * <p>
 * The symptom is quiet and actively misleading: tenancy builds billing's clients first, so when the pod registry
 * was unreachable the failure arrived as {@code BillingApiUnavailableException} — "the billing service could not be
 * reached" — while billing was healthy. An operator reading that during an incident is sent to the wrong service.
 * Cloning gives each client its own chain, so the catalog that translates a failure is the one belonging to the API
 * that actually failed.
 * </p>
 */
public final class WebClientsUtils {

    private WebClientsUtils() {
    }

    /**
     * @param errors the called API's error contract — typically a constant published by its {@code -external-api}
     *               module, such as {@code PaymentApiErrors.CATALOG}. May be {@code null} for an API that names none
     *               of its failures, in which case they arrive as {@code UnmappedRemoteFailureException}
     */
    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass, RemoteErrorCatalog errors) {
        WebClient.Builder configured = builder.clone().baseUrl(url);
        new S2sErrorHandler(errors).apply(configured);
        HttpExchangeAdapter clientAdapter = WebClientAdapter.create(configured.build());
        // No typed unwrapping on this path: the failure travels inside the returned Mono, where a proxy cannot rethrow
        // it as the method's declared checked type. A reactive caller uses onErrorMap; the advice still renders it.
        return buildClient(tClass, clientAdapter);
    }

    /**
     * @param errors the called API's error contract; may be {@code null} — see
     *               {@link #build(WebClient.Builder, String, Class, RemoteErrorCatalog)}
     */
    public static <T> T build(RestClient.Builder builder, String url, Class<T> tClass, RemoteErrorCatalog errors) {
        RestClient.Builder configured = builder.clone().baseUrl(url);
        new S2sErrorHandler(errors).apply(configured);
        HttpExchangeAdapter clientAdapter = RestClientAdapter.create(configured.build());
        return buildClient(tClass, clientAdapter);
    }

    private static <T> T buildClient(Class<T> tClass, HttpExchangeAdapter clientAdapter) {
        HttpServiceProxyFactory.Builder proxyBuilder = HttpServiceProxyFactory.builderFor(clientAdapter);
        proxyBuilder.customArgumentResolver(new LanguageCodeSerializeParamArgumentResolver())
                .customArgumentResolver(new StoreMerchantIdSerializeParamArgumentResolver())
                .customArgumentResolver(new StoreSerializeParamArgumentResolver())
                .customArgumentResolver(new OrgSerializeParamArgumentResolver())
                .customArgumentResolver(new DomainSerializeParamArgumentResolver());
        try {
            Class.forName("org.springframework.data.domain.Pageable");
            proxyBuilder.customArgumentResolver(new PageableSerializeParamArgumentResolver());
        } catch (Exception _) {
            // Ignore if Pageable is not on the classpath
        }
        return withTypedErrors(tClass, proxyBuilder.build().createClient(tClass));
    }

    /**
     * Wraps the generated proxy so a remote failure reaches the caller as the type the method declares, rather than as
     * the carrier it had to travel in. {@link S2sErrorHandler#declaredOrCarrier} holds the rule.
     */
    @SuppressWarnings("unchecked")
    private static <T> T withTypedErrors(Class<T> tClass, T target) {
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[] {tClass},
                (proxy, method, args) -> {
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw S2sErrorHandler.declaredOrCarrier(method, e.getCause());
                    }
                });
    }

}
