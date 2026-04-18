package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;
import java.util.Objects;

public record ManagerStoreDomain(String domain,
                                 DomainType domainType) implements Serializable, Comparable<ManagerStoreDomain> {
    @Override
    public int compareTo(ManagerStoreDomain o) {
        return Objects.compare(this.domain, o.domain, String.CASE_INSENSITIVE_ORDER);
    }
}
