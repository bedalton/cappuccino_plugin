package cappuccino.ide.intellij.plugin.extensions.plist

import com.intellij.openapi.fileTypes.LanguageFileType

import javax.swing.*

class ObjJPlistFileType private constructor() : LanguageFileType(ObjJPlistLanguage.INSTANCE) {

    override fun getName(): String {
        return "Objective-J Property Plist"
    }

    override fun getDescription(): String {
        return "An Objective-J plist file"
    }

    override fun getDefaultExtension(): String {
        return FILE_EXTENSION
    }

    override fun getIcon(): Icon? {
        return ObjJPlistIcons.DOCUMENT_ICON
    }

    companion object {
        val FILE_EXTENSION = "plist"
        val INSTANCE = ObjJPlistFileType()
    }
}
