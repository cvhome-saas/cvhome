package com.asrevo.cvhome.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The dialect is answered without a database, which is what lets {@code processAot} generate repositories at build
 * time.
 */
class PostgresJdbcConfigurationTest {

    @Test
    void theDialectIsPostgresAndTheDatabaseIsNeverAsked() {
        NamedParameterJdbcOperations operations = mock(NamedParameterJdbcOperations.class);

        assertThat(new PostgresJdbcConfiguration() {
        }.jdbcDialect(operations)).isSameAs(JdbcPostgresDialect.INSTANCE);
        verifyNoInteractions(operations);
    }

}
