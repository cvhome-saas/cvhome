package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.domain.ChallengeValidationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

import static com.asrevo.cvhome.certificatemanager.domain.ChallengeValidationType.*;
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


        Dns01Challenge dns01Challenge = authorization.findChallenge(Dns01Challenge.TYPE);
        if (dns01Challenge != null) {
            String key = String.format("_acme-challenge.%s", domain);
            String value = dns01Challenge.getDigest();
            challenges.add(new com.asrevo.cvhome.certificatemanager.domain.challenges.Dns01Challenge(key, value));
        }

        Http01Challenge http01Challenge = authorization.findChallenge(Http01Challenge.TYPE);
        if (http01Challenge != null) {
            String key = String.format("http://%s/.well-known/acme-challenge/%s", domain, http01Challenge.getToken());
            String value = http01Challenge.getAuthorization();
            challenges.add(new com.asrevo.cvhome.certificatemanager.domain.challenges.Http01Challenge(key, value));
        }

        TlsAlpn01Challenge tlsAlpn01Challenge = authorization.findChallenge(TlsAlpn01Challenge.TYPE);
        if (tlsAlpn01Challenge != null) {
            String value = encodeHexString(tlsAlpn01Challenge.getAcmeValidation());
            challenges.add(new com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpn01Challenge(domain, value));
        }
        return challenges;
    }

    @Transient
    @JsonIgnore
    public com.asrevo.cvhome.certificatemanager.domain.challenges.Dns01Challenge getDns01Challenge() {
        return (com.asrevo.cvhome.certificatemanager.domain.challenges.Dns01Challenge) getChallenge(Dns01);
    }

    @Transient
    @JsonIgnore
    public com.asrevo.cvhome.certificatemanager.domain.challenges.Http01Challenge getHttp01Challenge() {
        return (com.asrevo.cvhome.certificatemanager.domain.challenges.Http01Challenge) getChallenge(Http01);
    }

    @Transient
    @JsonIgnore
    public com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpn01Challenge getTlsAlpn01Challenge() {
        return (com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpn01Challenge) getChallenge(TlsAlpn01);
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
