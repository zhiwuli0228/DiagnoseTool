/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.nio.file.Path;

final class SidecarPathUtil {
    private SidecarPathUtil() {
    }

    static String displayName(Path path) {
        Path fileName = path == null ? null : path.getFileName();
        return fileName == null ? "local-source" : fileName.toString();
    }

    static String safeRelative(Path root, Path file) {
        return root.toAbsolutePath().normalize().relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    static boolean looksAbsolute(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.replace('\\', '/');
        return normalized.startsWith("/") || normalized.matches("^[A-Za-z]:/.*");
    }
}
