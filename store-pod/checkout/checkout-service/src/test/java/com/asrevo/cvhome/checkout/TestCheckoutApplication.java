package com.asrevo.cvhome.checkout;

import org.springframework.boot.SpringApplication;

public final class TestCheckoutApplication {
    private TestCheckoutApplication() {
    }

    static void main(String[] args) {
        SpringApplication.from(CheckoutApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
