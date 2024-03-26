package com.asrevo.cvhome.dcm;

import com.asrevo.cvhome.dcm.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.dcm.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties({
        AutoOrderDomainsProperties.class,
        DcmChallengesConfigProperties.class})
@Import(CvhomeSharedConfig.class)
public class DcmApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(DcmApplication.class, args);
    }

}
