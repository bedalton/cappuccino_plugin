package cappuccino.ide.intellij.plugin.lang

import cappuccino.ide.intellij.plugin.decompiler.lang.ObjJSjFileType
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class ObjJFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(fileTypeConsumer: FileTypeConsumer) {
        fileTypeConsumer.consume(ObjJFileType.INSTANCE, ObjJFileType.FILE_EXTENSION)
        fileTypeConsumer.consume(ObjJSjFileType.INSTANCE, ObjJSjFileType.FILE_EXTENSION)
        //fileTypeConsumer.consume(ObjJPlistFileType.INSTANCE, ObjJPlistFileType.FILE_EXTENSION);
    }
}
