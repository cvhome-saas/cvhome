package com.asrevo.cvhome.merchant.api;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The language-aware overload is a convenience over the single remote call: a store is returned in its default
 * language whatever the caller asks for, so the two must answer the same thing.
 */
class ExternalMerchantStoreServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    @Test
    void languageOverloadDelegatesToTheRemoteCall() {
        ReadableMerchantStore expected = new ReadableMerchantStore();
        ExternalMerchantStoreService client = store -> store.equals(STORE) ? expected : null;

        assertThat(client.getStore(STORE, new LanguageCode("fr"))).isSameAs(expected);
        assertThat(client.getStore(new StoreMerchantId("65f023632bc46470c104b75f"), null)).isNull();
    }

}
