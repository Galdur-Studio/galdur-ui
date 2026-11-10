package com.galdurstudio.galdurui.internal.bridge;

import java.util.List;

/** Matches screen classes against the player's exclusion list ({@code excludedScreens}). */
public final class ScreenFilter {
    private ScreenFilter() {
    }

    /**
     * {@return {@code true} if the entries name this class, one of its parent classes, or their package}
     *
     * @param type    screen class
     * @param entries class names or package names
     */
    public static boolean excludes(Class<?> type, List<? extends String> entries) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            if (excludes(current.getName(), entries)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return {@code true} if {@code className} is one of the entries, or in one of their packages}
     *
     * @param className fully qualified class name, {@code com.example.mymod.MachineScreen}
     * @param entries   class names or package names, {@code com.example.mymod}
     */
    public static boolean excludes(String className, List<? extends String> entries) {
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String prefix = trimmed.endsWith(".") ? trimmed : trimmed + ".";
            if (className.equals(trimmed) || className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
