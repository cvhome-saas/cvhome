package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.DnsChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Route53DnsChallengeInstaller implements ChallengeInstaller {
    private Route53Client route53Client;

    private Route53Client getClient() {
        if (this.route53Client == null) {
            this.route53Client = Route53Client.create();
        }
        return this.route53Client;
    }

    @Override
    public synchronized boolean setup(ChallengeInstall challenge) {

        Map<Domain, List<Challenge>> domainChallenges = challenge.challenges().stream()
                .collect(Collectors.groupingBy(Challenge::domain));

        return domainChallenges.entrySet().stream()
                .allMatch(it -> installDnsChallenge(it.getKey(), it.getValue()));
    }

    private boolean installDnsChallenge(Domain domain, List<Challenge> challenges) {
        return getClient().listHostedZones().hostedZones()
                .stream()
                .filter(it -> (domain.domain()+".").endsWith(it.name()))
                .findFirst()
                .map(zone -> {

                    List<ResourceRecord> records = challenges.stream()
                            .map(it -> ((DnsChallenge) it))
                            .map(it -> ResourceRecord.builder()
                                    .value("\"" + it.digest() + "\"")
                                    .build())
                            .toList();

                    ResourceRecordSet recordSet = ResourceRecordSet.builder()
                            .name(DnsChallenge.record(domain) + ".")
                            .type(RRType.TXT)
                            .ttl(1L)
                            .resourceRecords(records)
                            .build();

                    ChangeBatch changeBatch = ChangeBatch.builder()
                            .changes(Change.builder()
                                    .action(ChangeAction.UPSERT)
                                    .resourceRecordSet(recordSet)
                                    .build())
                            .build();

                    ChangeResourceRecordSetsRequest changeRequest = ChangeResourceRecordSetsRequest.builder()
                            .hostedZoneId(zone.id())
                            .changeBatch(changeBatch)
                            .build();

                    try {
                        getClient().changeResourceRecordSets(changeRequest);
                        log.info("successfully create txt record {} ", domain.domain());
                        return true;
                    } catch (Exception e) {
                        log.warn("error when creating txt record for {} with error {}", domain.domain(), e.getMessage());
                        return false;
                    }
                }).orElse(false);
    }

    @Override
    public boolean clean(ChallengeInstall challenge) {
        return false;
    }

    @Override
    public String provider() {
        return DnsProvider.ROUTE53.name();
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Dns01;
    }
}
