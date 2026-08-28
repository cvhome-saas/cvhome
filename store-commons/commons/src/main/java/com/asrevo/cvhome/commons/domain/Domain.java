package com.asrevo.cvhome.commons.domain;

public record Domain(String domain) {
    public boolean matches(String other) {
        return this.domain.equals(other);
    }
}
