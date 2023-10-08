package com.asrevo.cvhome.certificatemanager;

import com.asrevo.cvhome.certificatemanager.config.AutoOrderDomainsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AutoOrderDomainsProperties.class)
public class CertificateManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(CertificateManagerApplication.class, args);
    }

}
