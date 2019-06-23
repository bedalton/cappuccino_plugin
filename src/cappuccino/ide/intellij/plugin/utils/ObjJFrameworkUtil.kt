package cappuccino.ide.intellij.plugin.utils

import java.util.regex.Pattern

private val bundleRegex = Pattern.compile("<key>CPBundleName</key>\\s*<string>([^<]+?)</string>")

fun findFrameworkNameInPlistText(plistText:String) : String? {
    val matcher = bundleRegex.matcher(plistText)
    return if (matcher.find()) {
        val match = matcher.group(1)
        match
    } else {
        null
    }
}



fun createFrameworkSearchRegex(frameworkName:String) = """<key>CPBundleName</key>\s*<string>\s*$frameworkName\s*</string>""".toRegex()