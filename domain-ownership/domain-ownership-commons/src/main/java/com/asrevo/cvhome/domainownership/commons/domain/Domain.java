package com.asrevo.cvhome.domainownership.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.util.Arrays;
import java.util.stream.Stream;

public record Domain(String domain, @Column("domain_type") DomainType domainType) {
    public Domain {
        // @TODO should validate domain
    }

    @Transient
    public String getProvingDomain() {
        return "dos-prove." + this.domain;
    }

    @Transient
    @JsonIgnore
    public boolean isProvedTo(IdentityId identityId) {
        try {
            final Lookup lookup = new Lookup(getProvingDomain(), Type.TXT);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            final Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null && records.length > 0) {
                return Arrays.stream(records).flatMap(it -> {
                            if (it.getType() == 16) {
                                TXTRecord txtRecord = (TXTRecord) it;
                                return txtRecord.getStrings().stream();
                            } else {
                                return Stream.of();
                            }
                        })
                        .anyMatch(it -> it.equals(identityId.identity()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
