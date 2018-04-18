package org.cappuccino_project.ide.intellij.plugin.utils;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Strings {

    public static String notNull(@Nullable String string) {
        return notNull(string, "");
    }

    public static String notNull(@Nullable String string, @NotNull String defaultVal) {
        return string != null ? string : defaultVal;
    }

    public static String upperCaseFirstLetter(String string) {
        if (string == null) {
            return null;
        }
        if (string.length() < 2) {
            return string.toUpperCase();
        }
        return string.substring(0,1).toUpperCase() + string.substring(1);
    }

}
