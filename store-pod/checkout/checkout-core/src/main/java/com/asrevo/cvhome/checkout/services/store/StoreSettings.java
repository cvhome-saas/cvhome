package com.asrevo.cvhome.checkout.services.store;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

/**
 * The few things checkout needs to know about a store, read through the (cached) merchant client.
 */
@Component
@RequiredArgsConstructor
public class StoreSettings {

    private static final CurrencyCode FALLBACK_CURRENCY = new CurrencyCode("USD");

    private final ExternalMerchantStoreService merchantStores;

    public CurrencyCode currency(StoreMerchantId store) {
        ReadableMerchantStore merchantStore = merchantStores.getStore(store);
        return merchantStore == null || merchantStore.getCurrency() == null || merchantStore.getCurrency().code() == null
                ? FALLBACK_CURRENCY : merchantStore.getCurrency();
    }

    public boolean requiresLogin(StoreMerchantId store) {
        ReadableMerchantStore merchantStore = merchantStores.getStore(store);
        return merchantStore == null || merchantStore.isRequireLoginForOrderPlacement();
    }

    public Locale locale(LanguageCode language) {
        return LocaleUtils.getLocale(language == null ? LanguageCode.defaultLanguage() : language);
    }
}
