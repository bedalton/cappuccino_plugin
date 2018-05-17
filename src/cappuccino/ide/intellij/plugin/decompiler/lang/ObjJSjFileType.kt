package cappuccino.ide.intellij.plugin.decompiler.lang;

import cappuccino.ide.intellij.plugin.lang.ObjJIcons
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class ObjJSjFileType private constructor() : FileType {
        override fun isBinary(): Boolean = true

        override fun isReadOnly(): Boolean = true

        override fun getCharset(p0: VirtualFile, p1: ByteArray): String? = null

        override fun getName(): String =
                "Objective-J Compiled Script"

        override fun getDescription(): String =
                "An Objective-J compiled script file for the Cappuccino Web Framework"

        override fun getDefaultExtension(): String =
                FILE_EXTENSION

        override fun getIcon(): Icon? =
                ObjJIcons.DOCUMENT_ICON

        companion object {
                const val FILE_EXTENSION = "sj"
                val INSTANCE = ObjJSjFileType()
        }
}
