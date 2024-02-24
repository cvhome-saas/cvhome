package com.asrevo.cvhome.domaincertificatemanager.domain;


import com.asrevo.cvhome.domaincertificatemanager.commons.utils.Utils;

public record HttpValidationToken(String token) {
    public String encoded() {
        return Utils.encode64(token);
    }
}
