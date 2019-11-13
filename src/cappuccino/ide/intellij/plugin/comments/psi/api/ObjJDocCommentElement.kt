package cappuccino.ide.intellij.plugin.comments.psi.api

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.psi.PsiElement


interface ObjJDocCommentElement : PsiElement {

    val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            if (file == null) {
                //LOGGER.severe("Cannot get ObjJFile, as containing file is null.")
                return null
            }
            if (file is ObjJFile) {
                return file
            }
            //LOGGER.severe("ObjJFile is actually of type: " + this.containingFile.javaClass.simpleName)
            return null
        }
}