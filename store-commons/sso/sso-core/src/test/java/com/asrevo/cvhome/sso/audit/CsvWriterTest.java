package com.asrevo.cvhome.sso.audit;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** RFC 4180, and the one thing it does not say: a spreadsheet treats a leading = as a formula. */
class CsvWriterTest {

    private static final String QUOTED_EMPTY = "\"\"";

    @Test
    void quotesEveryFieldAndDoublesInnerQuotes() throws IOException {
        StringWriter out = new StringWriter();
        new CsvWriter(out).row(List.of("plain", "with,comma", "say \"hi\""));
        assertThat(out.toString()).isEqualTo("\"plain\",\"with,comma\",\"say \"\"hi\"\"\"\r\n");
    }

    @Test
    void writesEmptyForNull() throws IOException {
        StringWriter out = new StringWriter();
        new CsvWriter(out).row(Arrays.asList("a", null));
        assertThat(out.toString()).isEqualTo(String.format("\"a\",%s\r\n", QUOTED_EMPTY));
    }

    @Test
    void defusesAFormula() {
        assertThat(CsvWriter.field("=cmd|'/c calc'!A1")).isEqualTo("\"'=cmd|'/c calc'!A1\"");
        assertThat(CsvWriter.field("+1")).startsWith("\"'+");
        assertThat(CsvWriter.field("normal")).isEqualTo("\"normal\"");
    }

    @Test
    void keepsNewlinesInsideTheField() throws IOException {
        StringWriter out = new StringWriter();
        new CsvWriter(out).row(List.of("two\nlines"));
        assertThat(out.toString()).isEqualTo("\"two\nlines\"\r\n");
    }

}
