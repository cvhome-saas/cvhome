package com.asrevo.cvhome.subscription;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
class SubscriptionApplicationTests {

	@Test
	void contextLoads() {
	}

}
