package com.asrevo.cvhome.gateway;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
class StoreCoreGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
