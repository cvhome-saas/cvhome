package com.asrevo.cvhome.podregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.asrevo.cvhome.podregistry.config")
public final class PodRegistryApplication {

    private PodRegistryApplication() {
    }

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(PodRegistryApplication.class, args);
    }

}
