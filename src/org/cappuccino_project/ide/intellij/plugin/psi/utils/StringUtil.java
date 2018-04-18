package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import javax.annotation.Nullable;

public class StringUtil {

    public static boolean startsAndEndsWith (@Nullable String string, @Nullable String start, @Nullable String end) {
        return string != null && (start == null || string.startsWith(start)) && (end == null || string.endsWith(end));
    }
}
