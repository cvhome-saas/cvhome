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

public final class WebClientsUtils {

    private WebClientsUtils() {
    }

    /**
     * @param errors the called API's error contract — typically a constant published by its {@code -external-api}
     *               module, such as {@code PaymentApiErrors.CATALOG}. May be {@code null} for an API that names none
     *               of its failures, in which case they arrive as {@code UnmappedRemoteFailureException}
     */
    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass, RemoteErrorCatalog errors) {
        WebClient.Builder configured = builder.baseUrl(url);
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
        RestClient.Builder configured = builder.baseUrl(url);
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
