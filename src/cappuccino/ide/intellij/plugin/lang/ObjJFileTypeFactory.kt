@file:Suppress("DEPRECATION")

package cappuccino.ide.intellij.plugin.lang

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFileType
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class ObjJFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(fileTypeConsumer: FileTypeConsumer) {
        fileTypeConsumer.consume(ObjJFileType.INSTANCE, ObjJFileType.FILE_EXTENSION)
        fileTypeConsumer.consume(JsTypeDefFileType.INSTANCE, JsTypeDefFileType.FILE_EXTENSION)
        //fileTypeConsumer.consume(ObjJPlistFileType.instance, ObjJPlistFileType.FILE_EXTENSION);
    }
}
