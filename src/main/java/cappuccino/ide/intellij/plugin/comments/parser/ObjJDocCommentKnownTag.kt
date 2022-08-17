package cappuccino.ide.intellij.plugin.comments.parser

import com.intellij.openapi.util.text.StringUtil

enum class ObjJDocCommentKnownTag(val description:String, val isReferenceRequired: Boolean) {
    UNKNOWN("unknown", false),
    THROWS("throws", true),
    DEPRECATED("deprecated",false),
    EXCEPTION("exception", true),
    PARAM("param", true),
    CALLBACK("callback", true),
    RECEIVER("receiver", false),
    RETURN("return", true),
    SEE("see", true),
    SINCE("since", false),
    CONSTRUCTOR("constructor", false),
    PROPERTY("property", true),
    SAMPLE("sample", true),
    SUPPRESS("suppress", true),
    VAR("var", true),
    TYPEDEF("typedef", true),
    TYPE("type", true),
    ABSTRACT("This member must be implemented (or overridden) by the inheritor", true),
    ACCESS("Specify the access level of this member (private, package-private, public, or protected)", true),
    ALIAS("Treat a member as if it had a different name.", true),
    ASYNC("Indicate that a function is asynchronous", false),
    AUGMENTS("Indicate that a symbol inherits from, and adds to, a parent symbol", true),
    AUTHOR("Identify the author of an item", false),
    BORROWS("This object uses something from another object", false)
    ;

    val tagName:String by lazy {
        name.toLowerCase()
    }


    companion object {
        fun findByTagName(tagNameIn: CharSequence): ObjJDocCommentKnownTag? {
            var tagName = tagNameIn
            if (StringUtil.startsWith(tagName, "@")) {
                tagName = tagName.substring(1)
            }
            try {
                return valueOf(tagName.toString().toUpperCase())
            }
            catch (ignored: IllegalArgumentException) {
            }

            return null
        }
    }
}