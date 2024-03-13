package com.asrevo.cvhome.product;

import com.asrevo.cvhome.product.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class ProductApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
