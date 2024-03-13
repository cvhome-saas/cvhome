package com.asrevo.cvhome.domaincertificatemanager.service.verify.dns;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.DnsProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.DnsChallenge;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ListDnsChallenge;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import java.util.List;

@Slf4j
public class Route53DnsInstallerImpl implements DnsInstaller {
    private Route53Client route53Client;

    private Route53Client getClient() {
        if (this.route53Client == null) {
            this.route53Client = Route53Client.create();
        }
        return this.route53Client;
    }

    @Override
    public boolean install(ListDnsChallenge dnsChallenges) {
        if (!dnsChallenges.challenges().isEmpty()) {
            return getClient().listHostedZones().hostedZones()
                    .stream()
                    .filter(it -> (dnsChallenges.domain().domain() + ".").endsWith(it.name()))
                    .findFirst()
                    .map(HostedZone::id)
                    .map(hostedZoneId -> {
                        List<ResourceRecord> records = dnsChallenges.challenges().stream()
                                .map(it -> ResourceRecord.builder()
                                        .value("\"" + it.digest() + "\"")
                                        .build())
                                .toList();

                        ResourceRecordSet recordSet = ResourceRecordSet.builder()
                                .name(DnsChallenge.record(dnsChallenges.domain()) + ".")
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
                                .hostedZoneId(hostedZoneId)
                                .changeBatch(changeBatch)
                                .build();

                        try {
                            getClient().changeResourceRecordSets(changeRequest);
                            log.info("successfully create txt record {} ", dnsChallenges.domain().domain());
                            return true;
                        } catch (Exception e) {
                            log.warn("error when creating txt record for {} with error {}", dnsChallenges.domain().domain(), e.getMessage());
                            return false;
                        }
                    }).orElse(false);
        } else {
            return false;
        }
    }

    @Override
    public boolean clean(ListDnsChallenge dnsChallenges) {
        return false;
    }

    @Override
    public DnsProvider provider() {
        return DnsProvider.ROUTE53;
    }
}
