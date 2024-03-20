package com.asrevo.cvhome.wallet;

import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
public class WalletApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
    }

}
