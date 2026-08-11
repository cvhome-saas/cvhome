package com.asrevo.cvhome.billing;

import org.springframework.boot.SpringApplication;


public final class TestBillingApplication {

    private TestBillingApplication() {

    }

    static void main(String[] args) {
        SpringApplication.from(BillingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
