package com.asrevo.cvhome.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.asrevo.cvhome.content.config")
public final class ContentApplication {

    private ContentApplication() {
    }

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
