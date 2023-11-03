package com.asrevo.cvhome.domaincertificatemanager.config;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.dcm.challenge-config")
public class DcmChallengesConfigProperties {
    private DnsChallengeConfig dnsConfig;
    private HttpChallengeConfig httpConfig;
    private TlsAlpn01ChallengeConfig tlsAlpn01Config;

    public Set<String> providers(ChallengeValidationType type) {
        Set<String> results = new HashSet<>();
        switch (type) {
            case Dns01 -> results.addAll(this.dnsConfig.providers.stream().map(Enum::name).toList());
            case TlsAlpn01 -> results.addAll(Stream.of(this.tlsAlpn01Config.getProvider()).map(Enum::name).toList());
            case Http01 -> results.addAll(Stream.of(this.httpConfig.getProvider()).map(Enum::name).toList());
        }
        return results;
    }


    public enum FileProvider {
        S3, LOCAL
    }

    public enum DnsProvider {
        ROUTE53
    }

    @Getter
    @Setter
    private static class ChallengeConfig {
        private boolean enabled = true;
        private int order = 0;
    }

    @Getter
    @Setter
    private static abstract class FileChallengeConfig extends ChallengeConfig {
        private FileProvider provider;
        private S3FileProviderConfig s3Config;
        private LocalFileProviderConfig localConfig;
    }

    @Getter
    @Setter
    public static class DnsChallengeConfig extends ChallengeConfig {
        private List<DnsProvider> providers;
        private Route53ProviderConfig route53Config;
    }

    @Getter
    @Setter
    public static class HttpChallengeConfig extends FileChallengeConfig {

    }

    @Getter
    @Setter
    public static class TlsAlpn01ChallengeConfig extends FileChallengeConfig {

    }

    @Getter
    @Setter
    public static class S3FileProviderConfig {
        private String bucket;
    }

    @Getter
    @Setter
    public static class LocalFileProviderConfig {
        private String directory;
    }

    @Getter
    @Setter
    public static class Route53ProviderConfig {
        private String zoneId;
        private String matchingPattern = ".*";
    }
}
