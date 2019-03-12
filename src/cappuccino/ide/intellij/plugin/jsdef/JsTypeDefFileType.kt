package cappuccino.ide.intellij.plugin.jsdef

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class JsTypeDefFileType private constructor() : LanguageFileType(JsTypeDefLanguage.INSTANCE) {

    override fun getIcon(): Icon? {
        return null
    }

    override fun getName(): String {
        return "JsTypeDef"
    }

    override fun getDescription(): String {
        return "An javascript type definition file format to support objective-j coding"
    }

    override fun getDefaultExtension(): String {
        return FILE_EXTENSION
    }

    companion object {
        val FILE_EXTENSION = "ojstypedef"
        val INSTANCE = JsTypeDefFileType()
    }
}