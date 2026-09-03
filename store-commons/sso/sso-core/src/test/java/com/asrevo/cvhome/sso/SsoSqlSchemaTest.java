package com.asrevo.cvhome.sso;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Raw SQL in this library must never name a schema.
 *
 * <p>
 * uaa and cua are two deployments of this code against <em>one database</em>, separated by schema:
 * {@code spring.datasource.hikari.schema} is {@code ${spring.application.name}}, so every connection already
 * points at the right one and an unqualified table resolves per deployment. A qualifier hardcodes the other
 * deployment's data — the queries extracted from uaa said {@code uaa.oauth2_authorization} and
 * {@code uaa.audit_events}, which made cua's token revocation delete uaa's rows and cua's dashboard count
 * uaa's logins.
 * </p>
 *
 * <p>
 * The check is on source rather than on the compiled constants because the strings are {@code private static
 * final} inside services this test must not load; the failure mode it guards is a copied query, and a copied
 * query is visible in the source.
 * </p>
 */
class SsoSqlSchemaTest {

    /** {@code from x.y}, {@code join x.y}, {@code into x.y}, {@code update x.y} — an alias never follows these. */
    private static final Pattern QUALIFIED = Pattern.compile("(?i)\\b(from|join|into|update)\\s+(\\w+)\\.(\\w+)");

    /**
     * JPQL is exempt: {@code join u.roles r} walks an association, and a JPQL query names entities, which
     * Hibernate places with {@code default_schema}. A {@code nativeQuery} is not exempt — that is real SQL.
     */
    private static final Pattern JPQL = Pattern.compile("@Query\\((?!.*nativeQuery)");

    private static final Path SOURCES = Path.of("src/main/java");

    @Test
    void noQueryNamesASchema() throws IOException {
        assertThat(SOURCES).as("source root — a check that scans nothing passes vacuously").isDirectory();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            List<String> offenders = files.filter(path -> path.toString().endsWith(".java"))
                    .flatMap(SsoSqlSchemaTest::qualifiedReferences)
                    .toList();
            assertThat(offenders)
                    .as("schema-qualified table references; the connection's schema decides, not the query")
                    .isEmpty();
        }
    }

    private static Stream<String> qualifiedReferences(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read %s".formatted(file), e);
        }
        return java.util.stream.IntStream.range(0, lines.size())
                .filter(i -> !lines.get(i).stripLeading().startsWith("*"))
                .mapToObj(i -> report(file, i, lines.get(i)))
                .filter(java.util.Objects::nonNull);
    }

    private static String report(Path file, int index, String line) {
        if (JPQL.matcher(line).find()) {
            return null;
        }
        Matcher matcher = QUALIFIED.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        return "%s:%d %s.%s".formatted(file.getFileName(), index + 1, matcher.group(2), matcher.group(3));
    }

}
