package com.asrevo.cvhome.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.asrevo.cvhome.checkout.config")
public final class CheckoutApplication {

    private CheckoutApplication() {
    }

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(CheckoutApplication.class, args);
    }

}
