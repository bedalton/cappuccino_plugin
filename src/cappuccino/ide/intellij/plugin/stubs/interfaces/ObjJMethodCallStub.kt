package cappuccino.ide.intellij.plugin.stubs.interfaces


import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl
import com.intellij.psi.stubs.StubElement

interface ObjJMethodCallStub : StubElement<ObjJMethodCallImpl>, ObjJResolveableStub<ObjJMethodCallImpl> {

    val callTarget: String

    val possibleCallTargetTypes: List<String>

    val selectorStrings: List<String>

    val selectorString: String

    val containingClassName: String?

}
