package com.asrevo.cvhome.cua.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalBrandingService;
import com.asrevo.cvhome.content.model.site.MediaRef;
import com.asrevo.cvhome.content.model.site.SiteBranding;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import static org.mockito.ArgumentMatchers.any;

/**
 * The other pods cua talks to, stubbed. Without these the auth pages try to reach {@code lb://merchant} and
 * {@code lb://content} and leave the JVM.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ExternalClientsTestConfiguration {

    public static final String STORE_NAME = "Acme Supply Co.";

    public static final String LOGO_URL = "https://cdn.test/logo.png";

    @Bean
    @Primary
    ExternalMerchantStoreService stubExternalMerchantStoreService() {
        ExternalMerchantStoreService service = Mockito.mock(ExternalMerchantStoreService.class);
        Mockito.when(service.getStore(any())).thenAnswer(invocation -> {
            ReadableMerchantStore store = new ReadableMerchantStore();
            store.setId(invocation.getArgument(0, StoreMerchantId.class).getId());
            store.setName(STORE_NAME);
            return store;
        });
        return service;
    }

    /** A store with a logo, so the template's image branch is the one that renders. */
    @Bean
    @Primary
    ExternalBrandingService stubExternalBrandingService() {
        ExternalBrandingService service = Mockito.mock(ExternalBrandingService.class);
        Mockito.when(service.branding(any(), any()))
                .thenReturn(new SiteBranding(new MediaRef(1L, LOGO_URL, null, 320, 120), null, null, null));
        return service;
    }

}
