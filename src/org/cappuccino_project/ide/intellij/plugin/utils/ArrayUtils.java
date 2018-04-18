package org.cappuccino_project.ide.intellij.plugin.utils;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArrayUtils {

    public static final List<String> EMPTY_STRING_ARRAY = ImmutableList.copyOf(new String[0]);

    /**
     * Joins
     * @param list list of strings to join
     * @return string of string elements joined by delimiter
     */
    public static String join(@NotNull final List<String> list) {
        return join(list, ", ");
    }


    /**
     * Joins
     * @param list list of strings to join
     * @param delimiter joining string
     * @return string of string elements joined by delimiter
     */
    public static String join(@NotNull final List<String> list, @NotNull final String delimiter) {
        return join(list, delimiter, false);
    }

    /**
     * Joins
     * @param list list of strings to join
     * @param delimiter joining string
     * @return string of string elements joined by delimiter
     */
    public static String join(@NotNull final List<String> list, @NotNull final String delimiter, boolean trailing) {
        final StringBuilder builder = new StringBuilder();
        for (final String string : list) {
            builder.append(string).append(delimiter);
        }
        String out = builder.toString();
        if (!trailing && out.length() > delimiter.length()) {
            out = out.substring(0, out.length()-delimiter.length());
        }
        return out;
    }

    /**
     * Filters array list and returns a subset of items matching a given class
     * @param list list of items
     * @param filterClass class to filter items for
     * @param <T> type of element to return
     * @return list of items matching class
     */
    public static <T> List<T> filter(List<?> list, Class<T> filterClass) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> out = new ArrayList<>();
        for (Object ob : list) {
            if (filterClass.isInstance(ob)) {
                out.add(filterClass.cast(ob));
            }
        }
        return out;
    }

    public static <T> List<T> filter(@NotNull List<T> list, @NotNull Filter<T> filter) {
        List<T> out = new ArrayList<>();
        for (T item : list) {
            if (filter.check(item)) {
                out.add(item);
            }
        }
        return out;
    }


    public static interface Filter<T> {
        boolean check(T item);
    }


    @NotNull
    public static List<String> search(@NotNull List<String> keywords, @NotNull String queryString) {
        List<String> out = new ArrayList<>();
        if (queryString.isEmpty()) {
            return Collections.emptyList();
        }
        int queryStringLength = queryString.length();
        for (String keyword : keywords) {
            if (keyword.length()>=queryStringLength && keyword.contains(queryString)) {
                out.add(keyword);
            }
        }
        return out;
    }

    @NotNull
    public static <K,T> List<T> flatten(Map<K,List<T>> map) {
        List<T> out = new ArrayList<>();
        for (K key : map.keySet()) {
            out.addAll(map.get(key));
        }
        return out;
    }

}
