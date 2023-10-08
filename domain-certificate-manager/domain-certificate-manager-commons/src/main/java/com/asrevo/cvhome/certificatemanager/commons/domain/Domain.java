package com.asrevo.cvhome.certificatemanager.commons.domain;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

public record Domain(String domain) {
    public Domain {
        // @TODO should validate domain
    }

    private static String encode64(String value) {
        byte[] encoded = Base64.getEncoder().encode(value.getBytes());
        return new String(encoded);
    }

    @JsonIgnore
    public boolean isWildCard() {
        return this.domain.startsWith("*.");
    }

    @JsonIgnore
    public String encoded() {
        return encode64(domain);
    }


    @JsonIgnore
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
            Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null && records.length > 0) {
                return Arrays.stream(records).flatMap(it -> {
                            if (it.getType() == 16) {
                                TXTRecord txtRecord = (TXTRecord) it;
                                return txtRecord.getStrings().stream();
                            } else {
                                return Stream.of();
                            }
                        })
                        .anyMatch(it -> it.equals(identityId.id()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
