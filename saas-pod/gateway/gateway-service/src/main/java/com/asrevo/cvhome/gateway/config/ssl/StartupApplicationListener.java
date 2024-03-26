package com.asrevo.cvhome.gateway.config.ssl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


@Slf4j
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private final ResolverSSlProviderCacheLoader loader;

    public StartupApplicationListener(ResolverSSlProviderCacheLoader loader) {
        this.loader = loader;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loader.requestNewSslContext();
    }
}