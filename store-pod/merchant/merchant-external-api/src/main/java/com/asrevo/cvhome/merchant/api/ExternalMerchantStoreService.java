package com.asrevo.cvhome.merchant.api;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

@HttpExchange("/api/v1")
public interface ExternalMerchantStoreService {

    @GetExchange("store")
    ReadableMerchantStore getStore(StoreMerchantId store);

    default ReadableMerchantStore getStore(StoreMerchantId store, LanguageCode language) {
        return getStore(store);
    }

}
