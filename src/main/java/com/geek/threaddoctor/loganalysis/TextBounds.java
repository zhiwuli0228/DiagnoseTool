package com.geek.threaddoctor.loganalysis;

final class TextBounds {
    private TextBounds() {
    }

    static String limit(String text, int limit) {
        if (text == null || text.length() <= limit) {
            return text;
        }
        return text.substring(0, Math.max(0, limit)) + "...[truncated]";
    }
}
