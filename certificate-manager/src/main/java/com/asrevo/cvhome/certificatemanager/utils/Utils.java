package com.asrevo.cvhome.certificatemanager.utils;

import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

public class Utils {
    public static String encode64(String value) {
        byte[] encoded = Base64.getEncoder().encode(value.getBytes());
        return new String(encoded);
    }

    public static boolean performPreValidation(Map<String, Map<String, String>> challenges, String challenge) {
        try {
            Map<String, String> keyValueChallenge = challenges.get(challenge);
            String key = keyValueChallenge.get("key");
            String value = keyValueChallenge.get("value");
            if (Dns01Challenge.TYPE.equals(challenge)) {
                return performDns01Validation(key, value);
            }
            if (Http01Challenge.TYPE.equals(challenge)) {
                return performHttp01Validation(key, value);
            }
            if (TlsAlpn01Challenge.TYPE.equals(challenge)) {
                return performTlsAlpn01Validation(key, value);
            }

        } catch (Exception e) {

        }
        return false;
    }

    private static boolean performTlsAlpn01Validation(String key, String value) {
        return false;
    }

    private static boolean performHttp01Validation(String key, String value) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(key))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().contains(value);
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean performDns01Validation(String key, String value) {
        try {
            final Lookup lookup = new Lookup(key, Type.TXT);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            final Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null && records.length > 0) {
                return Arrays.stream(records).flatMap(it -> {
                            if (it.getType() == 16) {
                                TXTRecord txtRecord = (TXTRecord) it;
                                return txtRecord.getStrings().stream();
                            } else {
                                return Stream.of();
                            }
                        })
                        .anyMatch(it -> it.equals(value));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
