package com.asrevo.cvhome.commons.utils;

import java.util.Base64;

public class Utils {

    public static String encode64(String value) {
        byte[] encoded = Base64.getEncoder().encode(value.getBytes());
        return new String(encoded);
    }
}
