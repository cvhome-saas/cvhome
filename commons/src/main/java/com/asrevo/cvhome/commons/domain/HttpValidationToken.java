package com.asrevo.cvhome.commons.domain;


import com.asrevo.cvhome.commons.utils.Utils;

public record HttpValidationToken(String token) {
    public String encoded() {
        return Utils.encode64(token);
    }
}
