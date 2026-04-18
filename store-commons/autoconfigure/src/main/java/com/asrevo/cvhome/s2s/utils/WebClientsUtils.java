package com.asrevo.cvhome.s2s.utils;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebClientsUtils {

    private WebClientsUtils() {
    }

    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass) {
        WebClient client = builder.baseUrl(url).build();
        HttpExchangeAdapter clientAdapter = WebClientAdapter.create(client);
        return buildClient(tClass, clientAdapter);
    }

    public static <T> T build(RestClient.Builder builder, String url, Class<T> tClass) {
        RestClient client = builder.baseUrl(url).build();
        HttpExchangeAdapter clientAdapter = RestClientAdapter.create(client);
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
        } catch (Exception ignored) {
            // Ignore if Pageable is not on the classpath
        }
        return proxyBuilder.build().createClient(tClass);
    }

}
