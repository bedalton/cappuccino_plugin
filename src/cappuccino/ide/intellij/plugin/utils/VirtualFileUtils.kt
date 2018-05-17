package cappuccino.ide.intellij.plugin.utils;

import com.intellij.openapi.vfs.VirtualFile

object VirtualFileUtils {
    fun getText(virtualFile:VirtualFile) =
            virtualFile.contentsToByteArray().toString(virtualFile.charset)
}

fun VirtualFile.text() = VirtualFileUtils.getText(this)
