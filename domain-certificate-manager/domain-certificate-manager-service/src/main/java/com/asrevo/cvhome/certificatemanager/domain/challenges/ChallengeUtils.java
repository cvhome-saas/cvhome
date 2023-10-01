package com.asrevo.cvhome.certificatemanager.domain.challenges;

import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
public class ChallengeUtils {

    private static final TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }
    };

    static boolean validate(TlsAlpn01Challenge challenge) {
        return validateAcmeTls1(challenge.key(), 8443) || validateAcmeTls1(challenge.key(), 443);
    }

    private static SSLSocketFactory createSSlFactory() {
        if (socketFactory != null) return socketFactory;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL"); // OR TLS
            sslContext.init(null, new TrustManager[]{MOCK_TRUST_MANAGER}, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            return ((SSLSocketFactory) SSLSocketFactory.getDefault());
        }
    }    private static final SSLSocketFactory socketFactory = createSSlFactory();

    private static boolean validateAcmeTls1(String host, Integer port) {
        try (SSLSocket sslSocket = (SSLSocket) socketFactory.createSocket(InetAddress.getByName(host), port)) {
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            sslParameters.setApplicationProtocols(new String[]{org.shredzone.acme4j.challenge.TlsAlpn01Challenge.ACME_TLS_1_PROTOCOL});
            sslSocket.setSSLParameters(sslParameters);
            sslSocket.startHandshake();
            if (org.shredzone.acme4j.challenge.TlsAlpn01Challenge.ACME_TLS_1_PROTOCOL.equals(sslSocket.getApplicationProtocol())) {
                System.out.println(org.shredzone.acme4j.challenge.TlsAlpn01Challenge.ACME_TLS_1_PROTOCOL + " is supported");
                Certificate[] peerCertificates = sslSocket.getSession().getPeerCertificates();
                return true;
            }
        } catch (IOException ignored) {
        }
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
