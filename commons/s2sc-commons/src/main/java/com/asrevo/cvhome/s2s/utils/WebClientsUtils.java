package com.asrevo.cvhome.s2s.utils;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebClientsUtils {
    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass) {
        WebClient client = builder.baseUrl(url).build();

        WebClientAdapter clientAdapter = WebClientAdapter.create(client);
        HttpServiceProxyFactory.Builder proxyBuilder = HttpServiceProxyFactory.builderFor(clientAdapter);
        proxyBuilder.customArgumentResolver(new IdentifierSerializeParamArgumentResolver())
                .customArgumentResolver(new DomainSerializeParamArgumentResolver())
                .customArgumentResolver(new DomainReferenceSerializeParamArgumentResolver());
        try {
            Class.forName("org.springframework.data.domain.Pageable");
            proxyBuilder.customArgumentResolver(new PageableSerializeParamArgumentResolver());
        } catch (Exception ignored) {
        }
        return proxyBuilder.build().createClient(tClass);
    }
}
