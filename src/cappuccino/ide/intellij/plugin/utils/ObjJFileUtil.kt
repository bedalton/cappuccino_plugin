package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.File


val VirtualFile.contents:String get() {
    return VfsUtilCore.loadText(this)
}


object ObjJVirtualFileUtil {
    fun findFileByPath(path:String, refreshIfNecessary:Boolean = true) : VirtualFile? {
        val file = File(path)
        if (!file.exists())
            return null
        return VfsUtil.findFileByIoFile(file, refreshIfNecessary)
    }
}