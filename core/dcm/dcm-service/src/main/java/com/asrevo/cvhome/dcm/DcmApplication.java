package com.asrevo.cvhome.dcm;

import com.asrevo.cvhome.dcm.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.dcm.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.dcm.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AutoOrderDomainsProperties.class,
        ServiceDomainProperties.class,
        DcmChallengesConfigProperties.class})
public class DcmApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(DcmApplication.class, args);
    }

}
