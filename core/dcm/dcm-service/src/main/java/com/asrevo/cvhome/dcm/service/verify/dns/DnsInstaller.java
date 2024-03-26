package com.asrevo.cvhome.dcm.service.verify.dns;

import com.asrevo.cvhome.dcm.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.dcm.domain.challenges.ListDnsChallenge;


public interface DnsInstaller {
    DnsProvider provider();

    boolean install(ListDnsChallenge dnsChallenges);

    boolean clean(ListDnsChallenge dnsChallenges);
}
