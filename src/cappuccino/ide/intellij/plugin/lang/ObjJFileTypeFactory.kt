package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class ObjJFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(fileTypeConsumer: FileTypeConsumer) {
        fileTypeConsumer.consume(ObjJFileType.INSTANCE, ObjJFileType.FILE_EXTENSION)
        //fileTypeConsumer.consume(ObjJPlistFileType.INSTANCE, ObjJPlistFileType.FILE_EXTENSION);
    }
}
