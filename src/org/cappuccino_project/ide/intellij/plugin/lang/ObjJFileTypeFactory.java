package org.cappuccino_project.ide.intellij.plugin.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.ObjJPlistFile;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.ObjJPlistFileType;
import org.jetbrains.annotations.NotNull;

public class ObjJFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull
                                        FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(ObjJFileType.INSTANCE, ObjJFileType.FILE_EXTENSION);
        fileTypeConsumer.consume(ObjJPlistFileType.INSTANCE, ObjJPlistFileType.FILE_EXTENSION);
    }
}
