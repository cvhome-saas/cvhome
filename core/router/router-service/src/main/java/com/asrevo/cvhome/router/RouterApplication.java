package com.asrevo.cvhome.router;

import com.asrevo.cvhome.router.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class RouterApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(RouterApplication.class, args);
    }

}
