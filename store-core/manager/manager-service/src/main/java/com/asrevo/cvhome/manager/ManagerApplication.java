package com.asrevo.cvhome.manager;

import com.asrevo.cvhome.manager.config.KeycloakCredentialsProperties;
import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
@EnableConfigurationProperties({KeycloakCredentialsProperties.class})
public class ManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

}
