package com.asrevo.cvhome.gateway.utils;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebClientsUtils {
    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass) {
        WebClient client = builder.baseUrl(url).build();

        WebClientAdapter clientAdapter = WebClientAdapter.forClient(client);
        HttpServiceProxyFactory proxy = HttpServiceProxyFactory.builder(clientAdapter)

                .build();
        return proxy.createClient(tClass);
    }
}