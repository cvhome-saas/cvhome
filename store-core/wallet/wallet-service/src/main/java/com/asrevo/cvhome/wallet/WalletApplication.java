package com.asrevo.cvhome.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({})
public class WalletApplication {

  @lombok.Generated
  public static void main(String[] args) {
    SpringApplication.run(WalletApplication.class, args);
  }

}
