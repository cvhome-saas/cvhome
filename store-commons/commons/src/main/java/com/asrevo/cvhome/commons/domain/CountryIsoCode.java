package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record CountryIsoCode(String isoCode) implements Serializable, Comparable<CountryIsoCode> {
    public boolean isValid() {
        return isoCode != null && !isoCode.isEmpty();
    }

    @Override
    public int compareTo(CountryIsoCode o) {
        return this.isoCode.compareTo(o.isoCode);
    }
}
