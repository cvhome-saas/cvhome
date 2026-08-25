package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record ZoneCode(String code) implements Serializable, Comparable<ZoneCode> {
    @Override
    public int compareTo(ZoneCode o) {
        return this.code.compareTo(o.code);
    }
}
