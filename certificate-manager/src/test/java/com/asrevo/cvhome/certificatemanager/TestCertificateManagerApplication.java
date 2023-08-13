package com.asrevo.cvhome.certificatemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
class TestCertificateManagerApplication {

    public static void main(String[] args) {
        SpringApplication.from(CertificateManagerApplication::main).with(TestCertificateManagerApplication.class).run(args);
    }

}
