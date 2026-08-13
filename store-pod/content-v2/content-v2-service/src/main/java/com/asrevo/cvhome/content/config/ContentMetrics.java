package com.asrevo.cvhome.content.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class ContentMetrics {
    private static final String PUBLISH_LAG_QUERY = """
            select coalesce(max(extract(epoch from current_timestamp - publish_at)), 0)
              from content.content
             where status = 'SCHEDULED' and publish_at <= current_timestamp
            """;
    private static final String FAILED_MEDIA_QUERY = """
            select count(*) from content.media_asset
             where processing_status = 'FAILED' and deleted_at is null
            """;
    private static final String STORAGE_BYTES_QUERY = """
            select coalesce(sum(byte_size), 0) from content.media_asset
             where deleted_at is null
            """;
    private static final String STALE_TRANSLATIONS_QUERY = """
            select count(*) from content.content_description
             where translation_state = 'STALE'
            """;

    public ContentMetrics(MeterRegistry registry, JdbcTemplate jdbcTemplate) {
        gauge(registry, "content.scheduler.publish.lag.seconds", jdbcTemplate, PUBLISH_LAG_QUERY);
        gauge(registry, "content.media.failed", jdbcTemplate, FAILED_MEDIA_QUERY);
        gauge(registry, "content.storage.bytes", jdbcTemplate, STORAGE_BYTES_QUERY);
        gauge(registry, "content.translation.stale", jdbcTemplate, STALE_TRANSLATIONS_QUERY);
    }

    private static void gauge(MeterRegistry registry, String name, JdbcTemplate jdbcTemplate, String query) {
        Gauge.builder(name, jdbcTemplate, template -> value(template, query)).register(registry);
    }

    private static double value(JdbcTemplate jdbcTemplate, String query) {
        Number result = jdbcTemplate.queryForObject(query, Number.class);
        return result == null ? 0 : result.doubleValue();
    }
}
