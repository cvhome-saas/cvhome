package com.asrevo.cvhome.store;

import com.asrevo.cvhome.store.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class StoreApplication {

  @lombok.Generated
  public static void main(String[] args) {
    SpringApplication.run(StoreApplication.class, args);
  }

}
