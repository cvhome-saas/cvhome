package com.asrevo.cvhome.manager.entity;

import com.asrevo.cvhome.commons.domain.Domain;

import java.util.*;
import java.util.stream.Collectors;

class ManagerStoreDomains extends AbstractSet<ManagerStoreDomain> implements Set<ManagerStoreDomain> {
    private final Set<ManagerStoreDomain> domains;

    private ManagerStoreDomains(Collection<ManagerStoreDomain> domains) {
        this.domains = Set.copyOf(domains);
    }

    public static ManagerStoreDomains of(Collection<ManagerStoreDomain> domains) {
        return new ManagerStoreDomains(domains);
    }

    public static ManagerStoreDomains of(ManagerStoreDomain domain) {
        return new ManagerStoreDomains(Set.of(domain));
    }

    @Override
    public Iterator<ManagerStoreDomain> iterator() {
        return domains.iterator();
    }

    @Override
    public int size() {
        return domains.size();
    }

    public ManagerStoreDomains addDomain(Domain domain) {
        HashSet<ManagerStoreDomain> temp = new HashSet<>(this);
        temp.add(new ManagerStoreDomain(domain.domain(), AlisType.CUSTOM_DOMAIN));
        return of(temp);
    }

    public ManagerStoreDomains removeDomain(Domain domain) {
        Set<ManagerStoreDomain> temp = this.domains.stream()
                .filter(it -> !it.domain().equals(domain.domain()))
                .collect(Collectors.toSet());
        return of(temp);
    }

}

