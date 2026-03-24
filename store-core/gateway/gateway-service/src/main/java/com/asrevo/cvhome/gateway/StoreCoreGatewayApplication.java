package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
@EnableScheduling
public class StoreCoreGatewayApplication {

	@lombok.Generated
	public static void main(String[] args) {
		SpringApplication.run(StoreCoreGatewayApplication.class, args);
	}

}
