package com.asrevo.cvhome.testsupport.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Records the SQL statements Hibernate prepares on the calling thread while {@link #during} runs.
 *
 * <p>
 * A statement count per call is a property of the code, not of the load: the 2026-09-14 load test found the admin orders
 * list issuing 42 statements a request and a bulk upsert one read per sku, and neither needed a load test to see. A test
 * that pins the count keeps an N+1 from coming back. Only the calling thread is recorded, so scheduled jobs and outbox
 * pollers running beside the test never add to it; call the service bean directly rather than over HTTP, whose request
 * runs on a server thread.
 * </p>
 *
 * <p>
 * Every JPA integration test registers it through the test-support annotations ({@link #INSPECTOR}); outside
 * {@link #during} it records nothing.
 * </p>
 */
public final class SqlStatements implements StatementInspector {

    /** The property the test-support annotations set, so every JPA context carries the inspector. */
    public static final String INSPECTOR = """
            spring.jpa.properties.hibernate.session_factory.statement_inspector=\
            com.asrevo.cvhome.testsupport.sql.SqlStatements""";

    private static final ThreadLocal<List<String>> RECORDING = new ThreadLocal<>();

    @Override
    public String inspect(String sql) {
        List<String> recording = RECORDING.get();
        if (recording != null) {
            recording.add(sql);
        }
        return sql;
    }

    /** Runs {@code action} and returns what it returned with the statements it prepared on this thread, in order. */
    public static <T> Recorded<T> during(Callable<T> action) throws Exception {
        List<String> outer = RECORDING.get();
        List<String> recording = new ArrayList<>();
        RECORDING.set(recording);
        try {
            return new Recorded<>(action.call(), List.copyOf(recording));
        } finally {
            if (outer == null) {
                RECORDING.remove();
            } else {
                RECORDING.set(outer);
            }
        }
    }

    /**
     * What an action returned, and the statements it took.
     *
     * @param result     the action's return value
     * @param statements the SQL it prepared on the calling thread, in order
     */
    public record Recorded<T>(T result, List<String> statements) {

        public int count() {
            return statements.size();
        }

        /** How many statements start with {@code verb} ({@code select}, {@code insert}, …), ignoring case. */
        public long count(String verb) {
            String prefix = verb.toLowerCase(Locale.ROOT);
            return statements.stream()
                    .filter(sql -> sql.stripLeading().toLowerCase(Locale.ROOT).startsWith(prefix))
                    .count();
        }

        @Override
        public String toString() {
            return String.join(System.lineSeparator(), statements);
        }
    }
}
