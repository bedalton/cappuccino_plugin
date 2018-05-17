package cappuccino.ide.intellij.plugin.decompiler.lang

import cappuccino.ide.intellij.plugin.decompiler.decompiler.ObjJBinaryDecompiler
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.compiled.ClsStubBuilder
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.util.cls.ClsFormatException
import com.intellij.util.indexing.FileContent
import org.antlr.v4.runtime.RecognitionException

import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class ObjJSjClassStubBuilder : ClsStubBuilder() {

    override fun getStubVersion(): Int {
        return ObjJStubVersions.SOURCE_STUB_VERSION
    }

    @Throws(ClsFormatException::class)
    override fun buildFileStub(
            fileContent: FileContent): PsiFileStub<*>? {
        Logger.getLogger(ObjJSjClassStubBuilder::class.java.canonicalName).log(Level.INFO, "Parsing SJ from ObjJSjClassStubBuilder")
        try {
            val fileContents = ObjJBinaryDecompiler.decompileStatic(fileContent.file)
            val file = ObjJElementFactory.createFileFromText(fileContent.project, fileContent.fileName, fileContents.toString())
            return ObjJFileStubImpl(file, fileContent.fileName)
        } catch (e: IOException) {
            throw ClsFormatException("Decompile failed with IO error ", e)
        } catch(e:RecognitionException) {
            throw ClsFormatException(e.localizedMessage, e)
        }
    }
}
