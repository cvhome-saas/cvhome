package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.cache.annotation.CacheEvict;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** A cache annotation outside a {@code *Reads} class: refused. */
public class ProductService {

    @CacheEvict("fixture.product")
    public void save(StoreMerchantId store) {
        // nothing
    }
}
