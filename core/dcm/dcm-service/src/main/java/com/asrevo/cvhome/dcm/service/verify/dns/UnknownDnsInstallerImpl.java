package com.asrevo.cvhome.dcm.service.verify.dns;

import com.asrevo.cvhome.dcm.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.dcm.domain.challenges.ListDnsChallenge;
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