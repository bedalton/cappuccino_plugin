package cappuccino.ide.intellij.plugin.jstypedef.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.ObjJIcons
import javax.swing.Icon

class JsTypeDefFileType private constructor() : LanguageFileType(JsTypeDefLanguage.instance) {

    override fun getName(): String {
        return "JsTypeDef definitions file"
    }

    override fun getDescription(): String {
        return "A javascript definitions file for use with the Objective-J Plugin"
    }

    override fun getDefaultExtension(): String {
        return FILE_EXTENSION
    }

    override fun getIcon(): Icon? {
        return ObjJIcons.JSDEF_DOCUMENT_ICON
    }

    companion object {
        const val FILE_EXTENSION = "jstypedef"
        val INSTANCE = JsTypeDefFileType()
    }
}
