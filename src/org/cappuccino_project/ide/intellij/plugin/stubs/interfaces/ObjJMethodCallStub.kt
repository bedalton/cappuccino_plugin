package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces


import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl

interface ObjJMethodCallStub : StubElement<ObjJMethodCallImpl>, ObjJResolveableStub<ObjJMethodCallImpl> {

    val callTarget: String

    val possibleCallTargetTypes: List<String>

    val selectorStrings: List<String>

    val selectorString: String

    val containingClassName: String?

}
