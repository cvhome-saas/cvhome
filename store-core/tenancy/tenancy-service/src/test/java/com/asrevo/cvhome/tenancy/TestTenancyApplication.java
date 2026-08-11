package com.asrevo.cvhome.tenancy;

import org.springframework.boot.SpringApplication;

public final class TestTenancyApplication {

    private TestTenancyApplication() {

    }

    static void main(String[] args) {
        SpringApplication.from(TenancyApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
