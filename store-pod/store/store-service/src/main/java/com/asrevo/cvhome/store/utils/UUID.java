package com.asrevo.cvhome.store.utils;

import java.security.SecureRandom;

public class UUID {
    private static SecureRandom numberGenerator;

    public static byte[] generateRandomBytes() {
        return generateRandomBytes(16);
    }

    public static byte[] generateRandomBytes(int size) {
        SecureRandom ng = numberGenerator;
        if (ng == null) {
            numberGenerator = ng = new SecureRandom();
        }

        byte[] randomBytes = new byte[size];
        ng.nextBytes(randomBytes);
        return randomBytes;
    }
}
