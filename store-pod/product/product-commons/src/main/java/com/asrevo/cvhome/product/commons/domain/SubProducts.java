package com.asrevo.cvhome.product.commons.domain;

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

    public Boolean isEmpty() {
        return this.productIds.isEmpty();
    }

    public Integer size() {
        return this.productIds.size();
    }
}
