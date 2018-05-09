package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import java.util.logging.Level
import java.util.logging.Logger

interface ObjJCompositeElement : PsiElement {

    val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            if (file == null) {
                LOGGER.log(Level.SEVERE, "Cannot get ObjJFile, as containing file is null.")
                return null
            }
            if (file is ObjJFile) {
                return file
            }
            LOGGER.log(Level.SEVERE, "ObjJFile is actually of type: " + this.containingFile.javaClass.simpleName)
            return null
        }
    companion object {
        val LOGGER = Logger.getLogger(ObjJCompositeElement::class.java.name)
    }

    //ObjJCompositeElement getPsiOrParent();
}
