package com.asrevo.cvhome.commons.domain;

public record Domain(String domain) {
    public boolean equals(String other) {
        return this.domain.equals(other);
    }
}
