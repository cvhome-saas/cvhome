package com.asrevo.cvhome.tenancy.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.podregistry.services.pod.ExternalPodService;
import com.asrevo.cvhome.tenancy.manager.service.StorePodClientFactory;
import com.asrevo.cvhome.uaa.service.UserAccountService;

/**
 * Everything tenancy reaches for over the network, stubbed.
 *
 * <p>
 * Tenancy is the one service that talks to all of the others: uaa owns its users, billing decides whether an
 * organization may have another store, the pod registry decides where that store goes, and the pod itself holds the
 * store detail the console shows. Without these beans a single {@code POST /store-manager/private/store} would try
 * to reach four hosts that do not exist.
 * </p>
 *
 * <p>
 * Declared {@code @Primary} rather than {@code @MockitoBean} on purpose: every tenancy integration test imports this
 * one configuration, so they all share a single Spring context and a single Postgres container instead of forking a
 * context per class. The price is that the stubs are shared state, so a test that stubs one resets it first.
 * </p>
 */
@TestConfiguration
public class ExternalClientsTestConfiguration {

    @Bean
    @Primary
    UserAccountService stubUserAccountService() {
        return Mockito.mock(UserAccountService.class);
    }

    @Bean
    @Primary
    ExternalStoreQuotaService stubExternalStoreQuotaService() {
        return Mockito.mock(ExternalStoreQuotaService.class);
    }

    @Bean
    @Primary
    ExternalEntitlementService stubExternalEntitlementService() {
        return Mockito.mock(ExternalEntitlementService.class);
    }

    @Bean
    @Primary
    ExternalPodPlacementService stubExternalPodPlacementService() {
        return Mockito.mock(ExternalPodPlacementService.class);
    }

    @Bean
    @Primary
    ExternalPodService stubExternalPodService() {
        return Mockito.mock(ExternalPodService.class);
    }

    /**
     * The registry's pod map. Stubbed rather than left to run against the mocked {@link ExternalPodService}, because
     * {@code CachingPodDirectory} would otherwise fall back to the configuration seed and the answer would depend on
     * whichever pods the deployment happens to name.
     */
    @Bean
    @Primary
    CachingPodDirectory stubCachingPodDirectory() {
        return Mockito.mock(CachingPodDirectory.class);
    }

    /**
     * The factory that builds a merchant client per pod. Stubbed at the factory rather than at the HTTP layer so a
     * test can decide what one pod answers without standing a pod up.
     */
    @Bean
    @Primary
    StorePodClientFactory stubStorePodClientFactory() {
        return Mockito.mock(StorePodClientFactory.class);
    }

}
