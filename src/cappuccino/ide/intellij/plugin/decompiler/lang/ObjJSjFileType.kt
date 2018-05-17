package cappuccino.decompiler.lang;

import cappuccino.ide.intellij.plugin.lang.ObjJIcons
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

public class ObjJSjFileType private constructor() : LanguageFileType(ObjJLanguage.INSTANCE) {

        override fun getName(): String {
                return "Objective-J Compiled Script"
        }

        override fun getDescription(): String {
                return "An Objective-J compiled script file for the Cappuccino Web Framework"
        }

        override fun getDefaultExtension(): String {
                return FILE_EXTENSION
        }

        override fun getIcon(): Icon? {
                return ObjJIcons.DOCUMENT_ICON
        }

        companion object {
                val FILE_EXTENSION = "j"
                val INSTANCE = ObjJSjFileType()
        }
}
