package com.asrevo.cvhome.checkout.services.store;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreSettingsTest {

    private static final String EGP_2 = "EGP";

    private static final String USD_2 = "USD";

    @Mock
    private ExternalMerchantStoreService merchantStores;

    @InjectMocks
    private StoreSettings settings;

    @Test
    void readsCurrencyAndLoginRuleFromTheStore() {
        ReadableMerchantStore store = new ReadableMerchantStore();
        store.setCurrency(new CurrencyCode(EGP_2));
        store.setRequireLoginForOrderPlacement(false);
        when(merchantStores.getStore(Orders.STORE)).thenReturn(store);

        assertThat(settings.currency(Orders.STORE).code()).isEqualTo(EGP_2);
        assertThat(settings.requiresLogin(Orders.STORE)).isFalse();
    }

    @Test
    void failsSafeWhenTheStoreIsUnknownOrIncomplete() {
        when(merchantStores.getStore(Orders.STORE)).thenReturn(null);
        assertThat(settings.currency(Orders.STORE).code()).isEqualTo(USD_2);
        assertThat(settings.requiresLogin(Orders.STORE)).as("no store → require login").isTrue();

        ReadableMerchantStore noCurrency = new ReadableMerchantStore();
        noCurrency.setRequireLoginForOrderPlacement(true);
        when(merchantStores.getStore(Orders.STORE)).thenReturn(noCurrency);
        assertThat(settings.currency(Orders.STORE).code()).isEqualTo(USD_2);
        assertThat(settings.requiresLogin(Orders.STORE)).isTrue();
    }

    @Test
    void localeFollowsTheRequestLanguage() {
        assertThat(settings.locale(new LanguageCode("fr"))).isEqualTo(Locale.FRENCH);
        assertThat(settings.locale(null)).isEqualTo(Locale.ENGLISH);
    }
}
