package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

public record Challenges(List<Challenge> challenges) {

    public Challenges(Order order) {
        this(getChallenges(order));
    }

    public static List<Challenge> getChallenges(Order order) {
        List<Challenge> challenges = new ArrayList<>();

        Authorization authorization =
                order.getAuthorizations().stream().findFirst().orElseThrow();
        String domain = authorization.getIdentifier().getDomain();
        boolean isWildcard = authorization.isWildcard();


        Dns01Challenge dns01Challenge = authorization.findChallenge(Dns01Challenge.TYPE);
        if (dns01Challenge != null) {
            challenges.add(new DnsChallenge(authorization.getIdentifier().getDomain(), isWildcard, dns01Challenge.getDigest()));
        }

        Http01Challenge http01Challenge = authorization.findChallenge(Http01Challenge.TYPE);
        if (http01Challenge != null) {
            challenges.add(new HttpChallenge(authorization.getIdentifier().getDomain(), isWildcard, http01Challenge.getToken(), http01Challenge.getAuthorization()));
        }

        TlsAlpn01Challenge tlsAlpn01Challenge = authorization.findChallenge(TlsAlpn01Challenge.TYPE);
        if (tlsAlpn01Challenge != null) {
            challenges.add(new TlsAlpnChallenge(domain, isWildcard, encodeHexString(tlsAlpn01Challenge.getAcmeValidation())));
        }
        return challenges;
    }

    @Transient
    @JsonIgnore
    public DnsChallenge getDns01Challenge() {
        return (DnsChallenge) getChallenge(ChallengeValidationType.Dns01);
    }

    @Transient
    @JsonIgnore
    public HttpChallenge getHttp01Challenge() {
        return (HttpChallenge) getChallenge(ChallengeValidationType.Http01);
    }

    @Transient
    @JsonIgnore
    public TlsAlpnChallenge getTlsAlpn01Challenge() {
        return (TlsAlpnChallenge) getChallenge(ChallengeValidationType.TlsAlpn01);
    }

    @Transient
    @JsonIgnore
    public Challenge getChallenge(ChallengeValidationType challengeValidationType) {
        return challenges.stream().filter(it -> challengeValidationType.equals(it.type())).findFirst().orElse(null);
    }

    @Transient
    @JsonIgnore
    public boolean validate(ChallengeValidationType challengeValidationType) {
        return challenges.stream().filter(it -> it.type().equals(challengeValidationType)).allMatch(Challenge::validate);
    }
}
