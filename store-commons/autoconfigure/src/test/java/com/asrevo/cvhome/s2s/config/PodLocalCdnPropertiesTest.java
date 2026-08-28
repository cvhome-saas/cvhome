package com.asrevo.cvhome.s2s.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The local CDN base follows the MinIO the stack is actually running.
 *
 * <p>
 * It used to be spelled out a second time beside {@code s3-url}, which meant a stack that moved MinIO — {@code lcl
 * --stack} shifts its port by 1000 — served urls against a port nothing listened on. Seed scripts interpolate this
 * same key, so the drift reached the demo images too.
 * </p>
 */
class PodLocalCdnPropertiesTest {

    private static final String BASE_PATH = "com.asrevo.cvhome.cdn.base-path";

    private static final String S3_URL = "com.asrevo.cvhome.cdn.storage.s3-url";

    private static final String BUCKET = "d0dd4299-963a-4458-b31f-8efe31c35e8e";

    @Test
    void basePathDefaultsToTheConfiguredMinio() throws IOException {
        assertThat(podEnvironment(Map.of()).getProperty(BASE_PATH))
                .isEqualTo(String.format("http://localhost:9000/%s", BUCKET));
    }

    @Test
    void basePathFollowsAShiftedMinio() throws IOException {
        StandardEnvironment environment = podEnvironment(Map.of(S3_URL, "http://localhost:10000"));
        assertThat(environment.getProperty(BASE_PATH)).isEqualTo(String.format("http://localhost:10000/%s", BUCKET));
    }

    /** The pod layer's config file, under overrides of the kind {@code SPRING_APPLICATION_JSON} supplies. */
    private static StandardEnvironment podEnvironment(Map<String, Object> overrides) throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("overrides", overrides));
        List<PropertySource<?>> loaded = new YamlPropertySourceLoader()
                .load("store-pod-lcl-config", new ClassPathResource("store-pod-lcl-config.yml"));
        loaded.forEach(environment.getPropertySources()::addLast);
        return environment;
    }

}
