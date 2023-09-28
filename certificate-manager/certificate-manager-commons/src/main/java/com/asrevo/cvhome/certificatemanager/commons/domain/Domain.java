package com.asrevo.cvhome.certificatemanager.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Base64;

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
    public ChallengeValidationType getRecommendedChallengeValidationType() {
        return isWildCard() ? ChallengeValidationType.Dns01 : ChallengeValidationType.TlsAlpn01;
    }

    @JsonIgnore
    public String encoded() {
        return encode64(domain);
    }
}
