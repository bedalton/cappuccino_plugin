package cappuccino.ide.intellij.plugin.utils

import com.google.common.collect.ImmutableList

import java.util.ArrayList
import java.util.Collections

typealias Filter<T> = (T) -> Boolean
object ArrayUtils {

    val EMPTY_STRING_ARRAY: List<String> = ImmutableList.copyOf(arrayOfNulls(0))

    /**
     * Joins
     * @param list list of strings to join
     * @param delimiter joining string
     * @return string of string elements joined by delimiter
     */
    @JvmOverloads
    fun join(list: List<String>, delimiter: String = ", ", trailing: Boolean = false): String {
        val builder = StringBuilder()
        for (string in list) {
            builder.append(string).append(delimiter)
        }
        var out = builder.toString()
        if (!trailing && out.length > delimiter.length) {
            out = out.substring(0, out.length - delimiter.length)
        }
        return out
    }

    /**
     * Filters array list and returns a subset of items matching a given class
     * @param list list of items
     * @param filterClass class to filter items for
     * @param <T> type of element to return
     * @return list of items matching class
    </T> */
    fun <T> filter(list: List<*>?, filterClass: Class<T>): List<T> {
        if (list == null || list.isEmpty()) {
            return emptyList()
        }
        val out = ArrayList<T>()
        for (ob in list) {
            if (filterClass.isInstance(ob)) {
                out.add(filterClass.cast(ob))
            }
        }
        return out
    }

    fun <T> filter(list: List<T>, filter: Filter<T>): List<T> {
        val out = ArrayList<T>()
        for (item in list) {
            if (filter(item)) {
                out.add(item)
            }
        }
        return out
    }




    fun search(keywords: List<String>, queryString: String): List<String> {
        val out = ArrayList<String>()
        if (queryString.isEmpty()) {
            return mutableListOf()
        }
        val queryStringLength = queryString.length
        for (keyword in keywords) {
            if (keyword.length >= queryStringLength && keyword.contains(queryString)) {
                out.add(keyword)
            }
        }
        return out
    }

    fun <K, T> flatten(map: Map<K, List<T>>): List<T> {
        val out = ArrayList<T>()
        for (key in map.keys) {
            val list: List<T> = map[key] ?: continue
            out.addAll(list);
        }
        return out
    }

}
/**
 * Joins
 * @param list list of strings to join
 * @return string of string elements joined by delimiter
 */
/**
 * Joins
 * @param list list of strings to join
 * @param delimiter joining string
 * @return string of string elements joined by delimiter
 */
