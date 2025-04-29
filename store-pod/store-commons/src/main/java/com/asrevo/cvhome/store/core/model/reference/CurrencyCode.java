package com.asrevo.cvhome.store.core.model.reference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Currency;

public record CurrencyCode(String code) implements Serializable, Comparable<CurrencyCode> {
    @Override
    public int compareTo(CurrencyCode o) {
        return this.code.compareTo(o.code);
    }

    @JsonIgnore
    public Currency getCurrencyInstance() {
        return Currency.getInstance(code);
    }
}
