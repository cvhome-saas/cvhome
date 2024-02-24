package com.asrevo.cvhome.domaincertificatemanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.dcm.challenge-config")
public class DcmChallengesConfigProperties {
    private DnsChallengeInstallerInstallerConfig dnsInstallerConfig;
    private HttpChallengeInstallerInstallerConfig httpInstallerConfig;
    private TlsAlpn01ChallengeInstallerInstallerConfig tlsAlpn01InstallerConfig;


    private FileProvider acmFileProvider;

    private S3FileProviderConfig acmS3Config;
    private LocalFileProviderConfig acmLocalConfig;
    private TableFileProviderConfig acmTableConfig;


    public enum FileProvider {
        S3, LOCAL, TABLE, NONE
    }

    public enum DnsProvider {
        ROUTE53, UNKNOWN
    }

    @Getter
    @Setter
    private static class ChallengeInstallerConfig {
        private boolean enabled = true;
        private int order = 0;
    }

    @Getter
    @Setter
    private static abstract class FileChallengeInstallerConfig extends ChallengeInstallerConfig {
        private FileProvider installerFileProvider;
        private S3FileProviderConfig installerS3Config;
        private LocalFileProviderConfig installerLocalConfig;
        private TableFileProviderConfig installerTableConfig;
    }

    @Getter
    @Setter
    public static class DnsChallengeInstallerInstallerConfig extends ChallengeInstallerConfig {
        private List<DnsProvider> providers;
        private Route53ProviderConfig route53Config;
    }

    @Getter
    @Setter
    public static class HttpChallengeInstallerInstallerConfig extends FileChallengeInstallerConfig {

    }

    @Getter
    @Setter
    public static class TlsAlpn01ChallengeInstallerInstallerConfig extends FileChallengeInstallerConfig {

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
    public static class TableFileProviderConfig {
        private String directory;
    }

    @Getter
    @Setter
    public static class Route53ProviderConfig {
        private String zoneId;
        private String matchingPattern = ".*";
    }
}
