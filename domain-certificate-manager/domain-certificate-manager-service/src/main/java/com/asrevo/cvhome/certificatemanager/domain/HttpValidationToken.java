package com.asrevo.cvhome.certificatemanager.domain;


import com.asrevo.cvhome.certificatemanager.commons.utils.Utils;

public record HttpValidationToken(String token) {
    public String encoded() {
        return Utils.encode64(token);
    }
}
