package com.asrevo.cvhome.dcm.domain;


import com.asrevo.cvhome.dcm.commons.utils.Utils;

public record HttpValidationToken(String token) {
    public String encoded() {
        return Utils.encode64(token);
    }
}
