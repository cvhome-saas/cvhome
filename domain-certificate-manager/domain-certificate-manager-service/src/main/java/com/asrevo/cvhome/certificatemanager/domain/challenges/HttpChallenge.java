package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

public record HttpChallenge(String domain, boolean isWildCard, String token,
                            String authorization) implements Challenge {

    @Transient
    @JsonIgnore
    public String validationUrl() {
        return String.format("http://%s/.well-known/acme-challenge/%s", domain, token);
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Http01;
    }

    @Override
    public boolean validate() {
        return ChallengeUtils.validate(this);
    }
}
