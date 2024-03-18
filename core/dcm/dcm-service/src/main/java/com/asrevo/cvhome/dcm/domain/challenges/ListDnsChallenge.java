package com.asrevo.cvhome.dcm.domain.challenges;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;

import java.util.List;

public record ListDnsChallenge(Domain domain, List<DnsChallenge> challenges) implements Challenge {

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Dns01;
    }

    @Override
    public boolean validate() {
        return challenges.stream().allMatch(DnsChallenge::validate);
    }

    @Override
    public boolean isWildCard() {
        return challenges.stream().allMatch(DnsChallenge::isWildCard);
    }
}
