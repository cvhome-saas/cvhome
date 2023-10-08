package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.codec.DecoderException;
import org.shredzone.acme4j.Identifier;

import static org.apache.commons.codec.binary.Hex.decodeHex;

public record TlsAlpnChallenge(String domain, boolean isWildCard, String acmeValidationHexStr) implements Challenge {
    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.TlsAlpn01;
    }

    @Override
    public boolean validate() {
        return ChallengeUtils.validate(this);
    }

    @JsonIgnore
    public byte[] decode() {
        try {
            return decodeHex(acmeValidationHexStr);
        } catch (DecoderException e) {
            return null;
        }
    }

    public Identifier identifier() {
        return Identifier.dns(domain);
    }
}
