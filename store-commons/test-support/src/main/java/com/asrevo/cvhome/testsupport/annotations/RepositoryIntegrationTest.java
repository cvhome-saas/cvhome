package com.asrevo.cvhome.testsupport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * A JPA slice on a real Postgres container with the {@code test-stores} seed data, for the query classes an HTTP
 * test cannot steer (specifications, custom repository methods). Prefer {@link ServiceIntegrationTest} whenever the
 * endpoint can reach the query.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test-stores")
@Import(PostgresTestConfiguration.class)
public @interface RepositoryIntegrationTest {
}
