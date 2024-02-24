package com.asrevo.cvhome.domaincertificatemanager.service.verify.dns;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ListDnsChallenge;


public interface DnsInstaller {
    DnsProvider provider();

    boolean install(ListDnsChallenge dnsChallenges);

    boolean clean(ListDnsChallenge dnsChallenges);
}
