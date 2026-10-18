package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** A cached read that depends on who asks: refused. */
public class CartReads {

    /** Stands in for checkout's shopper id. */
    public record ShopperId(String sub) {
    }

    @Cacheable("fixture.cart")
    public String cart(StoreMerchantId store, ShopperId shopper) {
        return shopper.sub();
    }
}
