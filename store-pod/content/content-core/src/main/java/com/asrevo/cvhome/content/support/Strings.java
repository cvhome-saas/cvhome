package com.asrevo.cvhome.content.support;

public final class Strings {

    private Strings() {
    }

    public static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String abbreviate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

}
