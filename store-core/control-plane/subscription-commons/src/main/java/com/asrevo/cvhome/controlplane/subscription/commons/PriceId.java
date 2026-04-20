package com.asrevo.cvhome.controlplane.subscription.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

public record PriceId(String id) implements Identifier {
    @Override
    public Object getId() {
        return id;
    }
}
