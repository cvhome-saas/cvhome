package com.asrevo.cvhome.commons.domain.challenges;

import com.asrevo.cvhome.commons.domain.ChallengeValidationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

import static com.asrevo.cvhome.commons.utils.Utils.encode64;

public record Http01Challenge(String key, String value) implements Challenge {

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Http01;
    }

    @Override
    public boolean validate() {
        return ChallengeUtils.validate(this);
    }

    @Transient
    @JsonIgnore
    public String tokenEncoded() {
        String url = this.key();
        String[] urlParts = url.split("/");
        String token = urlParts[urlParts.length - 1];
        return encode64(token);
    }

}
