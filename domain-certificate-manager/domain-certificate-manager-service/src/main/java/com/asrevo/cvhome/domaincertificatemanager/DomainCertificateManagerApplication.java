package com.asrevo.cvhome.domaincertificatemanager;

import com.asrevo.cvhome.domaincertificatemanager.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.InstallersConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AutoOrderDomainsProperties.class,
        FileServiceConfigProperties.class,
        InstallersConfigProperties.class})
public class DomainCertificateManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(DomainCertificateManagerApplication.class, args);
    }

}
