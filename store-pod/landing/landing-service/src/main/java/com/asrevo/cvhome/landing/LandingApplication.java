package com.asrevo.cvhome.landing;

import com.asrevo.cvhome.landing.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class LandingApplication {

  @lombok.Generated
  public static void main(String[] args) {
    SpringApplication.run(LandingApplication.class, args);
  }

}
