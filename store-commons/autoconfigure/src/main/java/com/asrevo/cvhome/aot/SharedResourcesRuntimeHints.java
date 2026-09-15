package com.asrevo.cvhome.aot;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * The classpath resources every service reads by name, which a native image leaves out unless it is told.
 *
 * <p>
 * Spring Boot's own hints cover {@code application*.yml}, root {@code schema.sql} / {@code data.sql}, templates and
 * static assets. They do not cover the other YAML a service imports by name through {@code spring.config.import} —
 * the slices this module ships ({@code common-config.yml}, {@code lcl-config.yml}, {@code fargate-config.yml}, the
 * layer slices) and a service's own ({@code plan-catalog.yml} in billing) — nor the {@code init-sql/} scripts the
 * services name in {@code spring.sql.init.*-locations} and the {@code test-stores} profile loads by pattern. A missing
 * one is not a build error: the native service refuses to start with "Config data resource ... does not exist", or
 * starts and seeds nothing. Every YAML file at the classpath root is configuration, so all of them are included.
 * </p>
 *
 * <p>
 * Registered through {@code META-INF/spring/aot.factories} rather than on a bean: config data is loaded before the
 * application context exists, so no bean is there to carry the hint.
 * </p>
 */
public class SharedResourcesRuntimeHints implements RuntimeHintsRegistrar {

    /** Every YAML file at the classpath root: the shared slices, and any a service imports by name. */
    static final String ROOT_YAML = "*.yml";

    /** Schema, seed and test-store scripts, wherever a service keeps them under {@code init-sql/}. */
    static final String SQL_SCRIPTS = "init-sql/**";

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources()
                .registerPattern(ROOT_YAML)
                .registerPattern(SQL_SCRIPTS);
    }

}
