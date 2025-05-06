package com.asrevo.cvhome.s2s.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebClientsUtils {

    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass) {
        WebClient client = builder.baseUrl(url).build();
        HttpExchangeAdapter clientAdapter = WebClientAdapter.create(client);
        return buildClient(tClass, clientAdapter);
    }

    public static <T> T build(
            WebClient.Builder builder, String url, Class<T> tClass, ObjectMapper customMapper) {
        ExchangeStrategies strategies =
                ExchangeStrategies.builder()
                        .codecs(
                                configurer -> {
                                    configurer
                                            .defaultCodecs()
                                            .jackson2JsonEncoder(
                                                    new Jackson2JsonEncoder(customMapper));
                                    configurer
                                            .defaultCodecs()
                                            .jackson2JsonDecoder(
                                                    new Jackson2JsonDecoder(customMapper));
                                })
                        .build();

        WebClient client =
                builder.baseUrl(url)
                        .exchangeStrategies(strategies) // Apply the custom strategies
                        .build();

        HttpExchangeAdapter clientAdapter = WebClientAdapter.create(client);
        return buildClient(tClass, clientAdapter);
    }

    public static <T> T build(RestClient.Builder builder, String url, Class<T> tClass) {
        RestClient client = builder.baseUrl(url).build();
        HttpExchangeAdapter clientAdapter = RestClientAdapter.create(client);
        return buildClient(tClass, clientAdapter);
    }

    public static <T> T build(
            RestClient.Builder builder, String url, Class<T> tClass, ObjectMapper customMapper) {
        MappingJackson2HttpMessageConverter customJacksonConverter =
                new MappingJackson2HttpMessageConverter(customMapper);
        RestClient client =
                builder.baseUrl(url)
                        .messageConverters(
                                converters -> {
                                    converters.removeIf(
                                            converter ->
                                                    converter
                                                            instanceof
                                                            MappingJackson2HttpMessageConverter);
                                    converters.add(customJacksonConverter);
                                })
                        .build();

        HttpExchangeAdapter clientAdapter = RestClientAdapter.create(client);
        return buildClient(tClass, clientAdapter); // buildClient remains the same
    }

    private static <T> T buildClient(Class<T> tClass, HttpExchangeAdapter clientAdapter) {
        HttpServiceProxyFactory.Builder proxyBuilder =
                HttpServiceProxyFactory.builderFor(clientAdapter);
        proxyBuilder
                .customArgumentResolver(new LanguageCodeSerializeParamArgumentResolver())
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
