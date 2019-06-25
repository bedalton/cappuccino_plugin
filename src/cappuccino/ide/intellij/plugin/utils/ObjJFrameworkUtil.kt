package cappuccino.ide.intellij.plugin.utils

import java.util.regex.Pattern

private val bundleRegex = Pattern.compile("<key>CPBundleName</key>\\s*<string>([^<]+?)</string>")
private val isFramweworkPlistRegex = "<key>CPBundlePackageType</key>\\s*<string>FMWK</string>".toRegex()

val INFO_PLIST_FILE_NAME = "Info.plist"
val INFO_PLIST_FILE_NAME_TO_LOWER_CASE = INFO_PLIST_FILE_NAME.toLowerCase()

fun findFrameworkNameInPlistText(plistText:String) : String? {
    if (!isFramweworkPlistRegex.containsMatchIn(plistText))
        return null
    val matcher = bundleRegex.matcher(plistText)
    return if (matcher.find()) {
        val match = matcher.group(1)
        match
    } else {
        null
    }
}



fun createFrameworkSearchRegex(frameworkName:String) = """<key>CPBundleName</key>\s*<string>\s*$frameworkName\s*</string>""".toRegex()