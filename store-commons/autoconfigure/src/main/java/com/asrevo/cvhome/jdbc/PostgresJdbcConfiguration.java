package com.asrevo.cvhome.jdbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

/**
 * The base of every Spring Data JDBC service's {@code JdbcConfig}: PostgreSQL, stated rather than detected.
 *
 * <p>
 * {@link AbstractJdbcConfiguration#jdbcDialect} asks the live database which dialect it speaks. Spring Data generates
 * repository implementations ahead of time during {@code processAot}, and looks the dialect bean up to do it — at
 * build time, where there is no database, so the lookup instantiated the whole {@code DataSource} chain and failed the
 * build with "Failed to determine a suitable driver class". Every deployment runs PostgreSQL, so the detected answer
 * never varied; stating it also saves a round trip at every start. {@code @Lazy} keeps the unused argument from being
 * resolved at all.
 * </p>
 */
public abstract class PostgresJdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    @Override
    public JdbcDialect jdbcDialect(@Lazy NamedParameterJdbcOperations operations) {
        return JdbcPostgresDialect.INSTANCE;
    }

}
