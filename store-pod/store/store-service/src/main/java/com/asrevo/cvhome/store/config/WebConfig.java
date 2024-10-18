package com.asrevo.cvhome.store.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MerchantStoreArgumentResolver merchantStoreArgumentResolver;

    private final LanguageArgumentResolver languageArgumentResolver;

    public WebConfig(
            MerchantStoreArgumentResolver merchantStoreArgumentResolver,
            LanguageArgumentResolver languageArgumentResolver) {
        this.merchantStoreArgumentResolver = merchantStoreArgumentResolver;
        this.languageArgumentResolver = languageArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(merchantStoreArgumentResolver);
        argumentResolvers.add(languageArgumentResolver);
    }
}
