package com.asrevo.cvhome.dcm.service.installers.dns;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.dcm.domain.challenges.Challenge;
import com.asrevo.cvhome.dcm.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.dcm.domain.challenges.DnsChallenge;
import com.asrevo.cvhome.dcm.domain.challenges.ListDnsChallenge;
import com.asrevo.cvhome.dcm.service.ChallengeInstaller;
import com.asrevo.cvhome.dcm.service.verify.dns.DnsChallengeVerifyServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DnsChallengeInstaller implements ChallengeInstaller {
    private final DnsChallengeVerifyServiceProvider provider;

    public DnsChallengeInstaller(DnsChallengeVerifyServiceProvider provider) {
        this.provider = provider;
    }

    private static List<ListDnsChallenge> toListDnsChallenge(ChallengeInstall challenge) {
        return challenge.challenges().stream()
                .map(it -> ((DnsChallenge) it))
                .collect(Collectors.collectingAndThen(Collectors.groupingBy(Challenge::domain), domainListMap ->
                        domainListMap.entrySet().stream()
                                .map(it -> new ListDnsChallenge(it.getKey(), it.getValue())).toList()));
    }

    @Override
    public synchronized boolean setup(ChallengeInstall challenge) {
        List<ListDnsChallenge> dnsChallenges = toListDnsChallenge(challenge);
        return dnsChallenges
                .stream()
                .allMatch(it -> provider.getInstance(it).install(it));
    }

    @Override
    public boolean clean(ChallengeInstall challenge) {
        List<ListDnsChallenge> dnsChallenges = toListDnsChallenge(challenge);
        return dnsChallenges
                .stream()
                .allMatch(it -> provider.getInstance(it).clean(it));
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Dns01;
    }
}
