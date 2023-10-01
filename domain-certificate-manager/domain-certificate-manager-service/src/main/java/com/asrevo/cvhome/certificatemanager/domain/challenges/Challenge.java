package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;

public interface Challenge {
    String key();

    String value();

    ChallengeValidationType type();

    boolean validate();
}

