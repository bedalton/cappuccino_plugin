package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.PsiFileStubImpl
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFileStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.tree.IStubFileElementType

import java.util.logging.Logger
import java.util.regex.Pattern

class ObjJFileStubImpl(objJFile: ObjJFile?, override val fileName: String, override val imports: List<ObjJImportInfoStub>) : PsiFileStubImpl<ObjJFile>(objJFile), ObjJFileStub {

    override fun getType(): IStubFileElementType<out ObjJFileStub> {
        return ObjJStubTypes.FILE
    }

    companion object {
        private val LOGGER = Logger.getLogger("ObjJFileStubImpl")
        private val IMPORT_FILENAME_REGEX = Pattern.compile("(.*)?::(.*)")
    }
}


data class ObjJImportInfoStub(val framework:String?, val fileName:String?)