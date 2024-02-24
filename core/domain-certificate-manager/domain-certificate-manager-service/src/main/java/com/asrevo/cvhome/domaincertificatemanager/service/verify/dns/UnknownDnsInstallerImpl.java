package com.asrevo.cvhome.domaincertificatemanager.service.verify.dns;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ListDnsChallenge;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnknownDnsInstallerImpl implements DnsInstaller {

    @Override
    public boolean install(ListDnsChallenge dnsChallenges) {
        return false;
    }

    @Override
    public boolean clean(ListDnsChallenge dnsChallenges) {
        return false;
    }

    @Override
    public DnsProvider provider() {
        return DnsProvider.UNKNOWN;
    }
}