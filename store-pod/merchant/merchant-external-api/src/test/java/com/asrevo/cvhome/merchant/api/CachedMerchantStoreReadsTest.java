package com.asrevo.cvhome.merchant.api;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** The one cached client: it delegates, and its read is declared on the merchant client's region. */
@ExtendWith(MockitoExtension.class)
class CachedMerchantStoreReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    @Mock
    private ExternalMerchantStoreService delegate;

    @Mock
    private ReadableMerchantStore store;

    @Test
    void delegatesTheLookupAndNamesItsRegion() throws Exception {
        when(delegate.getStore(STORE)).thenReturn(store);

        assertThat(new CachedMerchantStoreReads(delegate).getStore(STORE)).isSameAs(store);
        Method read = CachedMerchantStoreReads.class.getMethod("getStore", StoreMerchantId.class);
        Cacheable cacheable = read.getAnnotation(Cacheable.class);
        assertThat(cacheable.cacheNames()).containsExactly(MerchantClientRegions.STORE_CLIENT.regionName());
        assertThat(cacheable.keyGenerator()).isEqualTo(StoreScopedKeyGenerator.BEAN);
        assertThat(MerchantClientRegions.STORE_CLIENT.valueType()).isEqualTo(ReadableMerchantStore.class);
        assertThat(MerchantClientRegions.STORE_CLIENT.ttl().toMinutes()).isEqualTo(5);
        assertThat(MerchantClientRegions.STORE_CLIENT.maxSize()).isEqualTo(10_000);
    }
}
