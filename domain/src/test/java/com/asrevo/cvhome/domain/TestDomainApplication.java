package com.asrevo.cvhome.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
class TestDomainApplication {

    public static void main(String[] args) {
        SpringApplication.from(DomainApplication::main).with(TestDomainApplication.class).run(args);
    }

}
