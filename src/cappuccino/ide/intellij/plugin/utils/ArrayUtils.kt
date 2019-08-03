package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.util.MultiValuesMap
import java.util.ArrayList

typealias Filter<T> = (T) -> Boolean
object ArrayUtils {

    val EMPTY_STRING_ARRAY: List<String> = emptyList()

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
}

fun List<String>.startsWith(prefix:String) : Boolean {
    this.forEach {
        if (it.startsWith(prefix))
            return true
    }
    return false;
}

fun List<String>.startsWithAny(prefixes:List<String>) : Boolean {
    prefixes.forEach {
        if (this.startsWith(it))
            return true
    }
    return false
}


fun <T> getFirstMatchOrNull(variableNameElements: List<T>, filter: Filter<T>): T? {
    return variableNameElements.firstOrNull(filter)
}
/*
fun <T> List<T>.plus(element:T) : List<T> {
    val list = toMutableList()
    list.add(element)
    return list
}

fun <T> List<T>.minus(element:T) : List<T> {
    if (!contains(element))
        return this
    val list = toMutableList()
    list.remove(element)
    return list
}*/

fun <Key, Type> MutableMap<Key, MutableList<Type>>.put(key:Key, item:Type) {
    if (!this.containsKey(key)) {
        this[key] = mutableListOf()
    }
    this[key]!!.add(item)
}

fun <T> Collection<T>?.isNotNullOrEmpty() : Boolean
    = this != null && this.isNotEmpty()
