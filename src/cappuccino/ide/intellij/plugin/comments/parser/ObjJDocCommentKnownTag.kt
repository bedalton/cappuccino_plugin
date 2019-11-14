package cappuccino.ide.intellij.plugin.comments.parser

import com.intellij.openapi.util.text.StringUtil

enum class ObjJDocCommentKnownTag(val isReferenceRequired:Boolean, val isSectionStart: Boolean) {
    UNKNOWN(false, false),
    AUTHOR(false, false),
    THROWS(true, false),
    EXCEPTION(true, false),
    PARAM(true, false),
    RECEIVER(false, false),
    RETURN(false, false),
    SEE(true, false),
    SINCE(false, false),
    CONSTRUCTOR(false, true),
    PROPERTY(true, true),
    SAMPLE(true, false),
    SUPPRESS(false, false);


    companion object {
        fun findByTagName(tagNameIn: CharSequence): ObjJDocCommentKnownTag? {
            var tagName = tagNameIn
            if (StringUtil.startsWith(tagName, "@")) {
                tagName = tagName.subSequence(1, tagName.length)
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