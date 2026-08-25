package com.asrevo.cvhome.merchant.api.v1;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalMerchantStoreApiTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    @Test
    void peerReadReturnsTheStoreInItsDefaultLanguage() {
        StoreFacade facade = mock(StoreFacade.class);
        ReadableMerchantStore expected = new ReadableMerchantStore();
        when(facade.getReadableMerchantStoreId(STORE)).thenReturn(expected);

        assertThat(new ExternalMerchantStoreApi(facade).getStore(STORE)).isSameAs(expected);
    }

}
