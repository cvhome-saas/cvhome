package com.asrevo.cvhome.payment;

import org.springframework.boot.SpringApplication;

public final class TestPaymentApplication {
    private TestPaymentApplication() {
    }

    static void main(String[] args) {
        SpringApplication.from(PaymentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
