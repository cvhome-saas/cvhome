package com.asrevo.cvhome.controlplane.controller;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.asrevo.cvhome.controlplane.TestcontainersConfiguration;
import com.asrevo.cvhome.controlplane.config.SecurityConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(AuthController.class)
@Import({ TestcontainersConfiguration.class, SecurityConfig.class })
@Tag("unit-test")
class AuthControllerTest {

	@Autowired
	WebTestClient client;

	@Test
	void current() {
		client.mutateWith(mockJwt().jwt((jwt) -> jwt.subject("test-subject")))
			.get()
			.uri("/api/v1/auth/current")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(String.class)
			.consumeWith(it -> {
				String responseBody = it.getResponseBody();
				System.out.println(responseBody);
			});
	}

	@Test
	void me() {
		client.mutateWith(mockJwt().jwt((jwt) -> jwt.subject("test-subject")))
			.get()
			.uri("/api/v1/auth/me")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(String.class)
			.consumeWith(it -> {
				String responseBody = it.getResponseBody();
				System.out.println(responseBody);
			});
	}

}
