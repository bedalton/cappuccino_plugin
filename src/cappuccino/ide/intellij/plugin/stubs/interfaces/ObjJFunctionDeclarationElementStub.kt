package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJFunctionDeclarationElementStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {
    val fileName: String
    val fqName: String
    val functionName: String
    val numParams: Int
    val paramNames: List<String>
    val returnType: String?
    val scope:ObjJFunctionScope
}

enum class ObjJFunctionScope(internal val intVal:Int) {
    GLOBAL_SCOPE(0),
    FILE_SCOPE(1),
    PRIVATE(2),
    PARAMETER_SCOPE(3),
    INVALID(-1);

    companion object {
        fun fromValue(value:Int) : ObjJFunctionScope {
            return when (value) {
                0 -> GLOBAL_SCOPE
                1 -> FILE_SCOPE
                2 -> PRIVATE
                3 -> PARAMETER_SCOPE
                else -> INVALID
            }

        }
    }
}