package com.asrevo.cvhome.domaincertificatemanager.domain.challenges;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

public record DnsChallenge(Domain domain, boolean isWildCard, String digest) implements Challenge {

    @Transient
    @JsonIgnore
    public String record() {
        return record(this.domain);
    }
    @Transient
    @JsonIgnore
    public static String record(Domain domain) {
        return String.format("_acme-challenge.%s", domain.domain());
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Dns01;
    }

    @Override
    public boolean validate() {
        return ChallengeUtils.validate(this);
    }
}
