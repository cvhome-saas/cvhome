package com.asrevo.cvhome.testsupport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.testsupport.security.ReactiveTestSecurityConfiguration;

/**
 * The integration test of a WebFlux service (the gateway): full context on a random port and a
 * {@code ReactiveJwtDecoder} that trusts {@code TestJwtSigner}. No database. Drive it with
 * {@code WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build()}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ReactiveTestSecurityConfiguration.class)
public @interface ReactiveIntegrationTest {
}
