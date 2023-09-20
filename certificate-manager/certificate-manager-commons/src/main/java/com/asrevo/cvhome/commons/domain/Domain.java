package com.asrevo.cvhome.commons.domain;

import java.util.Base64;

public record Domain(String domain) {
    public Domain {
        // @TODO should validate domain
    }

    private static String encode64(String value) {
        byte[] encoded = Base64.getEncoder().encode(value.getBytes());
        return new String(encoded);
    }

    public String encoded() {
        return encode64(domain);
    }
}
