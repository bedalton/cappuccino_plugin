package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.fileTypes.LanguageFileType

import javax.swing.*

class ObjJFileType private constructor() : LanguageFileType(ObjJLanguage.instance) {

    override fun getName(): String {
        return "Objective-J Script"
    }

    override fun getDescription(): String {
        return "An Objective-J script file for the Cappuccino Web Framework"
    }

    override fun getDefaultExtension(): String {
        return FILE_EXTENSION
    }

    override fun getIcon(): Icon? {
        return ObjJIcons.DOCUMENT_ICON
    }

    companion object {
        const val FILE_EXTENSION = "j"
        val INSTANCE = ObjJFileType()
    }
}
