package com.asrevo.cvhome.metrics;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Two aggregate queries against the table it was given, and nothing else: the table name is validated because it is
 * spliced into SQL.
 */
class JdbcOutboxSourceTest {

    private static final String TABLE = "catalog.outbox_record";

    private static final Instant OLDEST = Instant.parse("2026-09-06T10:00:00Z");

    private static final String NEW = "NEW";

    private static final String FAILED = "FAILED";

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);

    @Test
    void countsComeBackByStatusFromTheNamedTable() throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getString(1)).thenReturn(NEW, FAILED);
        when(row.getLong(2)).thenReturn(3L, 1L);
        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);
            handler.processRow(row);
            handler.processRow(row);
            return null;
        }).when(jdbc).query(contains(String.format("from %s group by status", TABLE)), any(RowCallbackHandler.class));

        Map<String, Long> counts = new JdbcOutboxSource(jdbc, TABLE).countsByStatus();

        assertThat(counts).containsExactly(Map.entry(NEW, 3L), Map.entry(FAILED, 1L));
    }

    @Test
    void oldestPendingIsTheMinimumCreatedAtOrEmpty() {
        JdbcOutboxSource source = new JdbcOutboxSource(jdbc, TABLE);
        when(jdbc.queryForObject(contains("completed_at is null and status <> 'FAILED'"), eq(Timestamp.class)))
                .thenReturn(Timestamp.from(OLDEST), (Timestamp) null);

        assertThat(source.oldestPending()).contains(OLDEST);
        assertThat(source.oldestPending()).isEmpty();
    }

    @Test
    void aTableNameThatIsNotOneIsRefused() {
        assertThatThrownBy(() -> new JdbcOutboxSource(jdbc, "outbox_record; drop table x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
