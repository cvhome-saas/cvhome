package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.domain.ChallengeValidationType;
import org.apache.commons.codec.DecoderException;
import org.shredzone.acme4j.Identifier;

import static org.apache.commons.codec.binary.Hex.decodeHex;

public record TlsAlpn01Challenge(String key, String value) implements Challenge {

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.TlsAlpn01;
    }

    @Override
    public boolean validate() {
        return ChallengeUtils.validate(this);
    }

    public byte[] decode() {
        try {
            return decodeHex(value);
        } catch (DecoderException e) {
            return null;
        }
    }

    public Identifier identifier() {
        return Identifier.dns(key);
    }
}
