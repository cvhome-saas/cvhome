package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;
import java.util.Currency;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
