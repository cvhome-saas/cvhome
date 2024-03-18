package com.asrevo.cvhome.store.commons.domain;

import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Iterator;
import java.util.List;

public record SubProducts(List<ProductId> productIds) implements Iterable<ProductId> {
    public SubProducts(ProductId... productIds) {
        this(List.of(productIds));
    }

    public static SubProducts empty() {
        return new SubProducts(List.of());
    }

    @Override
    public Iterator<ProductId> iterator() {
        return productIds.iterator();
    }

    @JsonIgnore
    public Boolean isEmpty() {
        return this.productIds.isEmpty();
    }

    @JsonIgnore
    public Integer size() {
        return this.productIds.size();
    }
}
