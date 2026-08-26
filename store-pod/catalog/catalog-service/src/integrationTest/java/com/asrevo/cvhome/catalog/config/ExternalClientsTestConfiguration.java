package com.asrevo.cvhome.catalog.config;

import java.util.List;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalMediaService;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.WeightUnit;

import static org.mockito.ArgumentMatchers.any;

/**
 * The other pods catalog talks to, stubbed.
 *
 * <p>
 * {@code ExternalMerchantStoreService} is not optional here: every product read asks it for the store's units of
 * measure, so without it a listing would try to reach {@code lb://merchant} and leave the JVM. Billing's
 * entitlement client answers "operable, no ceilings", which is what the guard interceptor and the product-count
 * ceiling both consult on a write. Content's media client answers for any asset id it is asked about, so the
 * attach path is exercised without standing a second service up; the stub records the usage calls so a test can
 * assert catalog told content what it holds.
 * </p>
 *
 * <p>
 * Declared {@code @Primary} rather than {@code @MockitoBean} on purpose: every catalog integration test imports the
 * same configuration, so they share one Spring context — and one Postgres and one MinIO container — instead of
 * forking a context per class.
 * </p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class ExternalClientsTestConfiguration {

    /** The store the {@code test-stores} seed data describes, as far as the catalog is concerned. */
    @Bean
    @Primary
    ExternalMerchantStoreService stubExternalMerchantStoreService() {
        ExternalMerchantStoreService service = Mockito.mock(ExternalMerchantStoreService.class);
        Mockito.when(service.getStore(any())).thenAnswer(invocation -> {
            ReadableMerchantStore store = new ReadableMerchantStore();
            store.setId(invocation.getArgument(0, StoreMerchantId.class).getId());
            store.setDimension(MeasureUnit.CM);
            store.setWeight(WeightUnit.KG);
            return store;
        });
        return service;
    }

    /**
     * Answers with an asset for every id asked about, so "the library has it" is the default and a test that
     * wants the opposite re-stubs it.
     */
    @Bean
    @Primary
    ExternalMediaService stubExternalMediaService() {
        ExternalMediaService service = Mockito.mock(ExternalMediaService.class);
        stubMediaDefaults(service);
        return service;
    }

    /**
     * The default answer: the library owns every id it is asked about.
     *
     * <p>
     * Exposed because the bean is shared by every catalog integration test — one Spring context, one set of
     * containers — so a test that stubs the opposite has to put this back, or it silently changes the meaning of
     * whatever runs next.
     * </p>
     */
    public static void stubMediaDefaults(ExternalMediaService service) {
        Mockito.doAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(1);
            return ids == null ? List.of() : ids.stream().map(id -> {
                ReadableMediaAsset asset = new ReadableMediaAsset();
                asset.setId(id);
                asset.setUrl(String.format("https://cdn.example/media/%d/asset.png", id));
                return asset;
            }).toList();
        }).when(service).resolve(any(), any());
    }

    @Bean
    @Primary
    ExternalEntitlementService stubExternalEntitlementService() throws Exception {
        ExternalEntitlementService service = Mockito.mock(ExternalEntitlementService.class);
        Mockito.when(service.snapshot(any())).thenAnswer(
                invocation -> EntitlementSnapshot.degradedOpen(invocation.getArgument(0, StoreMerchantId.class)));
        return service;
    }

}
