package com.asrevo.cvhome.payment.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;

/**
 * Other pods' {@code -external-api} clients, stubbed.
 *
 * <p>
 * Payment's outbox propagates every settled transaction to checkout, so a test that approves a payment would otherwise
 * try to reach {@code lb://checkout} and leave the JVM. Declared {@code @Primary} rather than {@code @MockitoBean} on
 * purpose: every payment integration test imports the same configuration, so they all share one Spring context and one
 * Postgres container instead of forking a context per class.
 * </p>
 */
@TestConfiguration
public class ExternalClientsTestConfiguration {

    @Bean
    @Primary
    ExternalOrderService stubExternalOrderService() {
        return Mockito.mock(ExternalOrderService.class);
    }

}
