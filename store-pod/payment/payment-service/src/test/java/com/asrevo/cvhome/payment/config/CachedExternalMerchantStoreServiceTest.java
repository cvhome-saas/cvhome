package com.asrevo.cvhome.payment.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedExternalMerchantStoreServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    @Mock
    private ExternalMerchantStoreService delegate;

    @Mock
    private ReadableMerchantStore store;

    @Test
    void delegatesTheLookup() {
        when(delegate.getStore(STORE)).thenReturn(store);

        assertThat(new CachedExternalMerchantStoreService(delegate).getStore(STORE)).isSameAs(store);
    }

}
