package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.charset.Charset
import java.util.logging.Logger

class ObjJProblemFileHighlighterFilter : Condition<VirtualFile> {

    /**
     * @throws java.io.IOException
     */
    override fun value(virtualFile: VirtualFile): Boolean {
        val fileType = virtualFile.fileType
        if (fileType !== ObjJFileType.INSTANCE)
            return false
        val file = File(virtualFile.canonicalPath)
        if (!file.exists()) {
            Logger.getLogger("#${ObjJProblemFileHighlighterFilter::class.java.simpleName}").severe("Failed to get file from virtual file")
            return false
        }
        val buffer = CharArray(STATIC_FILE_START_LENGTH)
        if (file.length() < STATIC_FILE_START_LENGTH)
            return true
        file.bufferedReader(Charset.defaultCharset(), DEFAULT_BUFFER_SIZE).read(buffer, 0, STATIC_FILE_START_LENGTH)
        return !buffer.contentEquals(STATIC_FILE_START.toCharArray())
    }

    companion object {
        private const val STATIC_FILE_START = "@STATIC;"
        private const val STATIC_FILE_START_LENGTH = STATIC_FILE_START.length
    }
}