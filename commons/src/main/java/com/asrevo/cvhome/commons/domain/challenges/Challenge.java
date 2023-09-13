package com.asrevo.cvhome.commons.domain.challenges;

import com.asrevo.cvhome.commons.domain.ChallengeValidationType;

public interface Challenge {
    String key();

    String value();

    ChallengeValidationType type();

    boolean validate();
}

