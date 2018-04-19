package org.cappuccino_project.ide.intellij.plugin.contributor;

import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ObjJKeywordsList {

    public static final List<String> keywords = Arrays.asList("break","do","case", "else", "new", "var", "catch", "finally", "return", "void","for", "continue", "switch", "while", "debugger", "function", "if", "throw", "delete", "var", "objj_msgSend", "YES", "yes", "NO", "no");

    @NotNull
    public static List<String> search(@NotNull String queryString) {
        return ArrayUtils.search(keywords, queryString);
    }
}
