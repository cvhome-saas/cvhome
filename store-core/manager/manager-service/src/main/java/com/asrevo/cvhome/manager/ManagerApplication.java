package com.asrevo.cvhome.manager;

import com.asrevo.cvhome.manager.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class ManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

}
