package com.asrevo.cvhome.inventory.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;

/**
 * Inventory's one outbound client, replaced: checkout is another pod and is never reachable from these tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ExternalClientsTestConfiguration {

    @Bean
    @Primary
    ExternalOrderService mockExternalOrderService() {
        return Mockito.mock(ExternalOrderService.class);
    }
}
