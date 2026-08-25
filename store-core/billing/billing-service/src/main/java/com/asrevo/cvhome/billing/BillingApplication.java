package com.asrevo.cvhome.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.asrevo.cvhome.billing.config")
public final class BillingApplication {

    private BillingApplication() {
    }

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }

}
