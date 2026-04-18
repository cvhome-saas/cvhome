package com.asrevo.cvhome.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
public class MerchantApplication {

    private MerchantApplication() {
    }

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
    }

}
