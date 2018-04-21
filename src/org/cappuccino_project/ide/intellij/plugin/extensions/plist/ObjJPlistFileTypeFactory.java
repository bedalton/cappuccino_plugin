package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class ObjJPlistFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull
                                        FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(ObjJPlistFileType.INSTANCE, ObjJPlistFileType.FILE_EXTENSION);
    }
}
