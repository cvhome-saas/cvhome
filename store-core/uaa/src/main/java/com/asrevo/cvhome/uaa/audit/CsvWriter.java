package com.asrevo.cvhome.uaa.audit;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * RFC 4180 in thirty lines: quote every field, double the quotes inside it, CRLF between rows.
 *
 * <p>
 * Quoting unconditionally rather than only when needed keeps the rule one line long and the output unambiguous —
 * and a leading {@code =} or {@code +} inside quotes is still a formula to a spreadsheet, so a value that starts with
 * one is prefixed with a quote character of its own.
 * </p>
 */
public final class CsvWriter {

    private static final String CRLF = "\r\n";

    private static final String QUOTE = "\"";

    private static final String ESCAPED_QUOTE = "\"\"";

    private static final String FORMULA_STARTERS = "=+-@";

    private final Writer out;

    public CsvWriter(Writer out) {
        this.out = out;
    }

    public void row(List<String> values) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(field(values.get(i)));
        }
        out.write(line.append(CRLF).toString());
    }

    static String field(String value) {
        String text = value == null ? "" : value;
        if (!text.isEmpty() && FORMULA_STARTERS.indexOf(text.charAt(0)) >= 0) {
            // A spreadsheet reads =cmd() as a formula even from a quoted CSV field; a leading quote makes it text.
            text = String.format("'%s", text);
        }
        return QUOTE + text.replace(QUOTE, ESCAPED_QUOTE) + QUOTE;
    }

}
