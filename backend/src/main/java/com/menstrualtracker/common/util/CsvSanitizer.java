package com.menstrualtracker.common.util;

/**
 * Guards against CSV formula injection. Any cell starting with =, +, -, @, tab,
 * or CR is prefixed with a single quote so spreadsheet clients treat it as text.
 */
public final class CsvSanitizer {

    private CsvSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@'
                || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }
}
