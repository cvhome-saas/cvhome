package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** A cached read as the rules want it: a {@code *Reads} class, keyed by store and language. */
public class ProductReads {

    @Cacheable("fixture.product")
    public String product(StoreMerchantId store, LanguageCode language, String slug) {
        return slug;
    }
}
