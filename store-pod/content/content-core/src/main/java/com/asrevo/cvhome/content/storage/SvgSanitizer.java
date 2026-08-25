package com.asrevo.cvhome.content.storage;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Strips the parts of an SVG that can run code or phone home: scripts, event handlers, foreignObject, external
 * references. Conservative regex pass — the file is still an SVG afterwards, just an inert one.
 */
public final class SvgSanitizer {

    private static final Pattern SCRIPT = Pattern.compile("(?is)<script.*?</script\\s*>|<script[^>]*/\\s*>");

    private static final Pattern FOREIGN = Pattern.compile("(?is)<foreignObject.*?</foreignObject\\s*>");

    private static final Pattern EVENT_ATTR = Pattern.compile("(?is)\\son[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");

    private static final Pattern EXTERNAL_HREF = Pattern.compile("(?is)\\s(xlink:href|href)\\s*=\\s*(\"|')(?!#|data:image/)[^\"']*\\2");

    private static final Pattern JS_URL = Pattern.compile("(?is)javascript:");

    private SvgSanitizer() {
    }

    public static byte[] clean(byte[] svg) {
        String s = new String(svg, StandardCharsets.UTF_8);
        s = SCRIPT.matcher(s).replaceAll("");
        s = FOREIGN.matcher(s).replaceAll("");
        s = EVENT_ATTR.matcher(s).replaceAll("");
        s = EXTERNAL_HREF.matcher(s).replaceAll("");
        s = JS_URL.matcher(s).replaceAll("");
        return s.getBytes(StandardCharsets.UTF_8);
    }

}
