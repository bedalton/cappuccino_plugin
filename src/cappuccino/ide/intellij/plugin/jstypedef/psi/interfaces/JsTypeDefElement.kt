package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import java.util.logging.Logger

interface JsTypeDefElement : ObjJUniversalPsiElement {

    val containingTypeDefFile: JsTypeDefFile?
        get() {
            val file = containingFile
            if (file == null) {
               //LOGGER.severe("Cannot get JsTypeDef file, as containing file is null.")
                return null
            }
            if (file is JsTypeDefFile) {
                return file
            }
           //LOGGER.severe("JsTypeDef file is actually of type: " + this.containingFile.javaClass.simpleName)
            return null
        }
    companion object {
        val LOGGER: Logger by lazy {Logger.getLogger(JsTypeDefElement::class.java.name)}
    }

    val containerName:String?

}
