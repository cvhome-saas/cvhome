package com.asrevo.cvhome.metrics;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Reads the outbox table (namastack's {@code outbox_record}) with two aggregate queries. A record is pending while it
 * has no {@code completed_at} and is not {@code FAILED}, whatever the intermediate status names are.
 */
public class JdbcOutboxSource implements OutboxMetrics.Source {

    private static final Pattern TABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?");

    private final JdbcTemplate jdbc;

    private final String countsSql;

    private final String oldestSql;

    public JdbcOutboxSource(JdbcTemplate jdbc, String table) {
        if (!TABLE_NAME.matcher(table).matches()) {
            throw new IllegalArgumentException(String.format("Not a table name: %s", table));
        }
        this.jdbc = jdbc;
        this.countsSql = String.format("select status, count(*) from %s group by status", table);
        this.oldestSql = String.format(
                "select min(created_at) from %s where completed_at is null and status <> 'FAILED'", table);
    }

    @Override
    public Map<String, Long> countsByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        jdbc.query(countsSql, rs -> {
            counts.put(rs.getString(1), rs.getLong(2));
        });
        return counts;
    }

    @Override
    public Optional<java.time.Instant> oldestPending() {
        Timestamp oldest = jdbc.queryForObject(oldestSql, Timestamp.class);
        return Optional.ofNullable(oldest).map(Timestamp::toInstant);
    }

}
