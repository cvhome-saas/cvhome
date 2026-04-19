package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import(ReactiveWebConfig.InternalReactiveWebConfig.class)
@SuppressWarnings("java:S1118")
public class ReactiveWebConfig {

    @Configuration
    static class InternalReactiveWebConfig implements WebFluxConfigurer {

        @Override
        public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
            ReactivePageableHandlerMethodArgumentResolver e = new ReactivePageableHandlerMethodArgumentResolver();
            e.setPageParameterName("page");
            e.setSizeParameterName("count");
            configurer.addCustomResolver(e);
            configurer.addCustomResolver(new ReactiveOrgStorePrincipalInfoArgumentResolver());
        }

    }

}
