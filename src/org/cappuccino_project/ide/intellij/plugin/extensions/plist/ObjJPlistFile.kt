package org.cappuccino_project.ide.intellij.plugin.extensions.plist

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class ObjJPlistFile(
        viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJPlistLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return ObjJPlistFileType.INSTANCE
    }


    override fun toString(): String {
        return "plist file"
    }
}
