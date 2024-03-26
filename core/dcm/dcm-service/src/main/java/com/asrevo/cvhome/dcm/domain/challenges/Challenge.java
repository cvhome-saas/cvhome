package com.asrevo.cvhome.dcm.domain.challenges;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;

public interface Challenge {
    Domain domain();

    ChallengeValidationType type();

    boolean validate();

    boolean isWildCard();
}

