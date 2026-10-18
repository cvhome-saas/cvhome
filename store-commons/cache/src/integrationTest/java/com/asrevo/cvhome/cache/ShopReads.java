package com.asrevo.cvhome.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** The cached read, as a service writes one. */
@Component
public class ShopReads {

    private final ShopRepository shops;

    public ShopReads(ShopRepository shops) {
        this.shops = shops;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ShopRegions.Names.NAME, keyGenerator = StoreScopedKeyGenerator.BEAN)
    public String name(StoreMerchantId store, LanguageCode language) {
        return shops.findById(store.getId()).map(Shop::getName).orElse(null);
    }
}
