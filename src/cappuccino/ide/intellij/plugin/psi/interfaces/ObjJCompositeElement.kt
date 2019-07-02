package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import java.util.logging.Level
import java.util.logging.Logger

interface ObjJCompositeElement : ObjJUniversalPsiElement<ObjJCompositeElement> {

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
        val LOGGER: Logger by lazy {Logger.getLogger(ObjJCompositeElement::class.java.name)}
    }
}