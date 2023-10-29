package com.asrevo.cvhome.domaincertificatemanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.installers")
public class InstallersConfigProperties {
    private Set<InstallerProvider> providers = Set.of(InstallerProvider.ROUTE53);

    public static enum InstallerProvider {
        LOCAL, S3, ROUTE53
    }
}
