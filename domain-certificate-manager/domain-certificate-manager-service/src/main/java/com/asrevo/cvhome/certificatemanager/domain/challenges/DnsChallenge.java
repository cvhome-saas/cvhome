package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

public record DnsChallenge(String domain, boolean isWildCard, String digest) implements Challenge {

    @Transient
    @JsonIgnore
    public String record() {
        return String.format("_acme-challenge.%s", domain);
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
