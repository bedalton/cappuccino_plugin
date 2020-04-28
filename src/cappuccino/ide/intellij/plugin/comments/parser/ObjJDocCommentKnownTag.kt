package cappuccino.ide.intellij.plugin.comments.parser

import com.intellij.openapi.util.text.StringUtil

enum class ObjJDocCommentKnownTag(val isReferenceRequired: Boolean) {
    UNKNOWN(false),
    AUTHOR(false),
    THROWS(true),
    DEPRECATED(false),
    EXCEPTION(true),
    PARAM(true),
    RECEIVER(false),
    RETURN(true),
    SEE(true),
    SINCE(false),
    CONSTRUCTOR(false),
    PROPERTY(true),
    SAMPLE(true),
    SUPPRESS(true),
    VAR(true);


    companion object {
        fun findByTagName(tagNameIn: CharSequence): ObjJDocCommentKnownTag {
            var tagName = tagNameIn
            if (StringUtil.startsWith(tagName, "@")) {
                tagName = tagName.substring(1)
            }
            try {
                return valueOf(tagName.toString().toUpperCase())
            }
            catch (ignored: IllegalArgumentException) {
            }

            return UNKNOWN
        }
    }
}