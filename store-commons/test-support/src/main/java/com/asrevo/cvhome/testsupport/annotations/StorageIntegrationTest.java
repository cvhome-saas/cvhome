package com.asrevo.cvhome.testsupport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.asrevo.cvhome.testsupport.containers.MinioTestConfiguration;
import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;
import com.asrevo.cvhome.testsupport.security.ServletTestSecurityConfiguration;

/**
 * {@link ServiceIntegrationTest} plus a MinIO container, for services that store media (catalog, content, merchant,
 * checkout, payment).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-stores")
@Import({PostgresTestConfiguration.class, MinioTestConfiguration.class, ServletTestSecurityConfiguration.class})
public @interface StorageIntegrationTest {
}
