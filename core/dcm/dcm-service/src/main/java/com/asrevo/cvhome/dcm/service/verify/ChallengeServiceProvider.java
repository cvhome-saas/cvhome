package com.asrevo.cvhome.dcm.service.verify;

import com.asrevo.cvhome.dcm.domain.challenges.Challenge;

public interface ChallengeServiceProvider<T extends Challenge, R> {
    R getInstance(T t);
}