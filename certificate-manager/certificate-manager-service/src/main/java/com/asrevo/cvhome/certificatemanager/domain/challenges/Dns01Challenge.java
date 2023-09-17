package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.domain.ChallengeValidationType;

public record Dns01Challenge(String key, String value) implements Challenge {

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Dns01;
    }

    @Override
    public boolean validate() {
        return ChallengeUtils.validate(this);
    }
}
