package com.asrevo.cvhome.testsupport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;
import com.asrevo.cvhome.testsupport.security.ServletTestSecurityConfiguration;

/**
 * The standard integration test of a servlet service: full context on a random port, a Postgres container, the
 * {@code test-stores} seed data, and a {@code JwtDecoder} that trusts {@code TestJwtSigner}.
 *
 * <pre>
 * &#64;ServiceIntegrationTest
 * class ProductApiIntegrationTest {
 *     &#64;LocalServerPort int port;
 *     &#64;Autowired TestJwtSigner signer;
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-stores")
@Import({PostgresTestConfiguration.class, ServletTestSecurityConfiguration.class})
public @interface ServiceIntegrationTest {
}
