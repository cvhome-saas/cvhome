package com.asrevo.cvhome.subscription;

import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
public class SubscriptionApplication {

	@lombok.Generated
	public static void main(String[] args) {
		SpringApplication.run(SubscriptionApplication.class, args);
	}

}
