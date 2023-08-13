package com.asrevo.cvhome.certificatemanager.utils;

import java.util.Base64;

public class Utils {
    public static String getDomainCode(String domain) {
        byte[] encoded = Base64.getEncoder().encode(domain.getBytes());
        return new String(encoded);
    }
}
