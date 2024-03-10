package com.asrevo.cvhome.wallet;

import com.asrevo.cvhome.wallet.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class WalletApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
    }

}
