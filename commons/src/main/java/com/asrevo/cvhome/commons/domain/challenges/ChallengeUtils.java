package com.asrevo.cvhome.commons.domain.challenges;

import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Stream;

public class ChallengeUtils {


    static boolean validate(TlsAlpn01Challenge challenge) {
        return false;
    }

    static boolean validate(Dns01Challenge challenge) {
        try {
            final Lookup lookup = new Lookup(challenge.key(), Type.TXT);
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
                        .anyMatch(it -> it.equals(challenge.value()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    static boolean validate(Http01Challenge challenge) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(challenge.key()))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().contains(challenge.value());
        } catch (Exception ignored) {
        }
        return false;
    }
}
