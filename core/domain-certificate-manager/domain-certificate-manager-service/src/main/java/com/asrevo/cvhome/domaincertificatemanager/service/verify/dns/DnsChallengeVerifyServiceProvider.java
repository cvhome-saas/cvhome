package com.asrevo.cvhome.domaincertificatemanager.service.verify.dns;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ListDnsChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.verify.ChallengeServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class DnsChallengeVerifyServiceProvider implements ChallengeServiceProvider<ListDnsChallenge, DnsInstaller> {

    private final Map<DnsProvider, DnsInstaller> instances = new HashMap<>();

    @Override
    public DnsInstaller getInstance(ListDnsChallenge challenge) {
        return getRecommendedProvider(challenge)
                .map(it ->
                        instances.computeIfAbsent(it, (provider) ->
                                getDnsInstaller(it)))
                .orElse(null);
    }

    private static DnsInstaller getDnsInstaller(DnsProvider it) {
        return switch (it) {
            case ROUTE53 -> new Route53DnsInstallerImpl();
            case UNKNOWN -> new UnknownDnsInstallerImpl();
        };
    }

    //@TODO later we could provide mechanism to determine real dns provider that host this domain
    private Optional<DnsProvider> getRecommendedProvider(ListDnsChallenge challenge) {
        return Optional.of(DnsProvider.ROUTE53);
    }
}