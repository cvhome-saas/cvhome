package com.asrevo.cvhome.certificatemanager.domain.challenges;

import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

import static org.shredzone.acme4j.challenge.TlsAlpn01Challenge.ACME_TLS_1_PROTOCOL;

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

    static boolean validate(TlsAlpnChallenge challenge) {
        return validateAcmeTls1(challenge.domain().domain(), 8443) || validateAcmeTls1(challenge.domain().domain(), 443);
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
    }

    /*
    *
    010001fc03034ca42010c0fb1debb720cd12134e838ae8cf2e1bc67fe71690563430e437ad562044d135cd69b09ef628dd21e7d089b2e576249f574c18536d3891bd16f5d4f5fa0062130213011303c02cc02bcca9c030cca8c02f009fccaa00a3009e00a2c024c028c023c027006b006a00670040c02ec032c02dc031c026c02ac025c029c00ac014c009c0130039003800330032c005c00fc004c00e009d009c003d003c0035002f00ff0100014100000010000e00000b676174657761792e636f6d000500050100000000000a00160014001d001700180019001e01000101010201030104000b000201000010000d000b0a61636d652d746c732f31001100090007020004000000000017000000230000000d002c002a040305030603080708080804080508060809080a080b0401050106010402030303010302020302010202002b00050403040303002d000201010032002c002a040305030603080708080804080508060809080a080b04010501060104020303030103020203020102020033006b0069001d002019f4729f8b7e31d2d59583dc4ae9fed21f715c2f4994d44c02cf97cb283e20290017004104de7b45ae38a78b1ec545093ce80b68c4d2fd782cd989caf87ac755f14606cffc0001a2187a9b16600c32a172f82eee807ed955b18b60592b6ef213976f63cce4
    byte[] bytes = new byte[clientHello
            .readableBytes()];
    int readerIndex = clientHello
            .readerIndex();
    clientHello
    .getBytes(readerIndex, bytes);
    System.out.println(ByteArrayUtil.toHexString(bytes));
    * **************
//        byte[] bytes = ByteArrayUtil.hexStringToByteArray("03034ca42010c0fb1debb720cd12134e838ae8cf2e1bc67fe71690563430e437ad562044d135cd69b09ef628dd21e7d089b2e576249f574c18536d3891bd16f5d4f5fa0062130213011303c02cc02bcca9c030cca8c02f009fccaa00a3009e00a2c024c028c023c027006b006a00670040c02ec032c02dc031c026c02ac025c029c00ac014c009c0130039003800330032c005c00fc004c00e009d009c003d003c0035002f00ff0100014100000010000e00000b676174657761792e636f6d000500050100000000000a00160014001d001700180019001e01000101010201030104000b000201000010000d000b0a61636d652d746c732f31001100090007020004000000000017000000230000000d002c002a040305030603080708080804080508060809080a080b0401050106010402030303010302020302010202002b00050403040303002d000201010032002c002a040305030603080708080804080508060809080a080b04010501060104020303030103020203020102020033006b0069001d002019f4729f8b7e31d2d59583dc4ae9fed21f715c2f4994d44c02cf97cb283e20290017004104de7b45ae38a78b1ec545093ce80b68c4d2fd782cd989caf87ac755f14606cffc0001a2187a9b16600c32a172f82eee807ed955b18b60592b6ef213976f63cce4");
//        ByteBuffer wrap = ByteBuffer.wrap(bytes);
//        TLSClientHelloExtractor tlsClientHelloExtractor = new TLSClientHelloExtractor(wrap);
//        System.out.println(tlsClientHelloExtractor);
    * */
    public static void main(String[] args) throws IOException {
        boolean b = validateAcmeTls1("aswwa.asrevo.com", 443);
    }

    private static boolean validateAcmeTls1(String host, Integer port) {
        if (isPortOpen(host, port)) {
            try (SSLSocket sslSocket = (SSLSocket) socketFactory.createSocket(InetAddress.getByName(host), port)) {
                SSLParameters sslParameters = sslSocket.getSSLParameters();
                sslParameters.setApplicationProtocols(new String[]{ACME_TLS_1_PROTOCOL});
                sslSocket.setSSLParameters(sslParameters);
                sslSocket.startHandshake();
                if (ACME_TLS_1_PROTOCOL.equals(sslSocket.getApplicationProtocol())) {
                    System.out.println(ACME_TLS_1_PROTOCOL + " is supported");
                    Certificate[] peerCertificates = sslSocket.getSession().getPeerCertificates();
                    return true;
                }
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    private static final SSLSocketFactory socketFactory = createSSlFactory();

    private static boolean isPortOpen(String hostName, int portNumber) {
        boolean result;
        try {
            Socket s = new Socket();
            InetSocketAddress endpoint = new InetSocketAddress(hostName, portNumber);
            s.connect(endpoint, 1000);
            s.close();
            result = true;
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    static boolean validate(DnsChallenge challenge) {
        try {
            final Lookup lookup = new Lookup(challenge.record(), Type.TXT);
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
                        .anyMatch(it -> it.equals(challenge.digest()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    static boolean validate(HttpChallenge challenge) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(challenge.validationUrl()))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().contains(challenge.authorization());
        } catch (Exception ignored) {
        }
        return false;
    }


}
