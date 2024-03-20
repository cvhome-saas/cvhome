package com.asrevo.cvhome.landing.utils;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebClientsUtils {
    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass) {
        WebClient client = builder.baseUrl(url).build();

        WebClientAdapter clientAdapter = WebClientAdapter.create(client);
        HttpServiceProxyFactory proxy = HttpServiceProxyFactory.builderFor(clientAdapter)
                .customArgumentResolver(new IdentifierSerializeParamArgumentResolver())
                .customArgumentResolver(new PageableSerializeParamArgumentResolver())
                .build();
        return proxy.createClient(tClass);
    }
}
