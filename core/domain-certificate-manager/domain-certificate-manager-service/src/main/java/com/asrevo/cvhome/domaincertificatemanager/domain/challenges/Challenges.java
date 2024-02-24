package com.asrevo.cvhome.domaincertificatemanager.domain.challenges;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

public record Challenges(List<Challenge> challenges) {

    public Challenges(Order order) {
        this(getChallenges(order));
    }

    public static List<Challenge> getChallenges(Order order) {
        return order.getAuthorizations().stream().flatMap(it -> getChallenges(it).stream()).collect(Collectors.toList());
    }

    private static List<Challenge> getChallenges(Authorization authorization) {
        List<Challenge> challenges = new ArrayList<>();
        String domain = authorization.getIdentifier().getDomain();
        boolean isWildcard = authorization.isWildcard();


        Dns01Challenge dns01Challenge = authorization.findChallenge(Dns01Challenge.TYPE);
        if (dns01Challenge != null) {
            challenges.add(new DnsChallenge(new Domain(domain), isWildcard, dns01Challenge.getDigest()));
        }

        Http01Challenge http01Challenge = authorization.findChallenge(Http01Challenge.TYPE);
        if (http01Challenge != null) {
            challenges.add(new HttpChallenge(new Domain(domain), isWildcard, http01Challenge.getToken(), http01Challenge.getAuthorization()));
        }

        TlsAlpn01Challenge tlsAlpn01Challenge = authorization.findChallenge(TlsAlpn01Challenge.TYPE);
        if (tlsAlpn01Challenge != null) {
            challenges.add(new TlsAlpnChallenge(new Domain(domain), isWildcard, encodeHexString(tlsAlpn01Challenge.getAcmeValidation())));
        }
        return challenges;
    }

    @Transient
    @JsonIgnore
    public ChallengeInstall getInstallChallenge(ChallengeValidationType challengeValidationType) {
        return new ChallengeInstall(challenges.stream().filter(it -> challengeValidationType.equals(it.type())).toList());
    }

    @Transient
    @JsonIgnore
    public boolean validate(ChallengeValidationType validationType) {
        return challenges.stream().filter(it -> it.type().equals(validationType)).allMatch(Challenge::validate);
    }
}
