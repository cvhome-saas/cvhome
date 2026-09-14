package com.asrevo.cvhome.testsupport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

/**
 * A full context on a Postgres container with the {@code test-stores} seed data, and <em>no</em> test JWT decoder.
 * This is the shape for the two authorization servers (uaa, cua): they issue the tokens, so they own their own
 * {@code JwtDecoder} and overriding it makes the context ambiguous. Everything else wants
 * {@link ServiceIntegrationTest}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = SqlStatements.INSPECTOR)
@ActiveProfiles("test-stores")
@Import(PostgresTestConfiguration.class)
public @interface DatabaseIntegrationTest {
}
