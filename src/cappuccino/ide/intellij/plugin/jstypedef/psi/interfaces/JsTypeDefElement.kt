package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import com.intellij.psi.PsiElement
import java.util.logging.Level
import java.util.logging.Logger

interface JsTypeDefElement : PsiElement {

    val containingTypeDefFile: JsTypeDefFile?
        get() {
            val file = containingFile
            if (file == null) {
                LOGGER.log(Level.SEVERE, "Cannot get JsTypeDef file, as containing file is null.")
                return null
            }
            if (file is JsTypeDefFile) {
                return file
            }
            LOGGER.log(Level.SEVERE, "JsTypeDef file is actually of type: " + this.containingFile.javaClass.simpleName)
            return null
        }
    companion object {
        val LOGGER: Logger by lazy {Logger.getLogger(JsTypeDefElement::class.java.name)}
    }

    fun <PsiT : PsiElement> getParentOfType(parentClass:Class<PsiT>) : PsiT?
    fun <PsiT : PsiElement> getChildOfType(childClass:Class<PsiT>) : PsiT?
    fun <PsiT : PsiElement> getChildrenOfType(childClass:Class<PsiT>) : List<PsiT>

    val containerName:String?

}
