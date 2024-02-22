package com.asrevo.cvhome.domaincertificatemanager.service.installers.dns;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.DnsChallenge;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ListDnsChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.domaincertificatemanager.service.verify.dns.DnsChallengeVerifyServiceProvider;
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

    private static List<ListDnsChallenge> toListDnsChallenge(ChallengeInstall challenge) {
        return challenge.challenges().stream()
                .map(it -> ((DnsChallenge) it))
                .collect(Collectors.collectingAndThen(Collectors.groupingBy(Challenge::domain), domainListMap ->
                        domainListMap.entrySet().stream()
                                .map(it -> new ListDnsChallenge(it.getKey(), it.getValue())).toList()));
    }
}