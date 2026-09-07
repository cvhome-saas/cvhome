package com.asrevo.cvhome.s2s.config.internal;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Which unsafe-method requests a read-only impersonation may still make.
 *
 * <p>
 * A read that travels as a {@code POST} — a statistic query with a body, a paged list with a filter object — is a
 * read all the same, and refusing it would blank the dashboard for the very person sent to look at it. The list
 * is short, Ant-style, matched against the service-local path, and lives in {@code common-config.yml} so every
 * service agrees on it. A missing entry shows as a 403 on a read screen, never as a write that slipped through.
 * </p>
 *
 * @param allowedWrites Ant patterns of paths whose unsafe methods are reads
 */
@ConfigurationProperties(prefix = "com.asrevo.cvhome.s2s.read-only")
public record ReadOnlyActorProperties(List<String> allowedWrites) {

    public ReadOnlyActorProperties {
        allowedWrites = allowedWrites == null ? List.of() : List.copyOf(allowedWrites);
    }

}
