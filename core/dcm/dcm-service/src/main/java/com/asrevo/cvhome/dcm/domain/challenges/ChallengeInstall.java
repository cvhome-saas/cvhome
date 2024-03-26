package com.asrevo.cvhome.dcm.domain.challenges;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;

import java.util.List;
import java.util.stream.Collectors;

public record ChallengeInstall(List<Challenge> challenges) {

    public ChallengeInstall {
        int typeSize = challenges.stream().map(Challenge::type).collect(Collectors.toSet()).size();
        if (typeSize != 1) {
            throw new RuntimeException("ChallengeInstall only handle same challenges type");
        }
        int domainSize = challenges.stream().map(Challenge::domain).collect(Collectors.toSet()).size();
        if (domainSize != 1) {
            throw new RuntimeException("ChallengeInstall only handle same challenges domain");
        }
    }

    public ChallengeValidationType challengeType() {
        if (challenges.isEmpty()) {
            return ChallengeValidationType.Dns01;
        } else if (challenges.size() == 1) {
            return challenges.get(0).type();
        } else {
            return challenges.stream()
                    .filter(Challenge::isWildCard)
                    .findFirst()
                    .map(Challenge::type)
                    .orElseGet(() -> challenges.get(0).type());
        }
    }

    public String domain() {
        return this.challenges.stream().findFirst().map(it -> it.domain().domain()).orElse("");
    }
}
