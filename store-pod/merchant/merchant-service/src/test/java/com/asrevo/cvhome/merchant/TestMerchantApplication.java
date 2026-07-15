package com.asrevo.cvhome.merchant;

import org.springframework.boot.SpringApplication;

public class TestMerchantApplication {
    private TestMerchantApplication() {

    }

    static void main(String[] args) {
        SpringApplication.from(MerchantApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
