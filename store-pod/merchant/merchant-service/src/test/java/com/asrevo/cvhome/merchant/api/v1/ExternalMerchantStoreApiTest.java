package com.asrevo.cvhome.merchant.api.v1;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.reads.MerchantStoreReads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalMerchantStoreApiTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    @Test
    void peerReadReturnsTheStoreInItsDefaultLanguage() {
        MerchantStoreReads reads = mock(MerchantStoreReads.class);
        ReadableMerchantStore expected = new ReadableMerchantStore();
        when(reads.store(STORE)).thenReturn(expected);

        assertThat(new ExternalMerchantStoreApi(reads).getStore(STORE)).isSameAs(expected);
    }

}
