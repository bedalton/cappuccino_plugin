package cappuccino.ide.intellij.plugin.extensions.plist

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class ObjJPlistFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(fileTypeConsumer: FileTypeConsumer) {
        fileTypeConsumer.consume(ObjJPlistFileType.INSTANCE, ObjJPlistFileType.FILE_EXTENSION)
    }
}
