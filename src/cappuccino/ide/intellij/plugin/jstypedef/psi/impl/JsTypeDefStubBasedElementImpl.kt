/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cappuccino.ide.intellij.plugin.jstypedef.psi.impl

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefLanguage
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModule
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefStubBasedElement
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.*
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.PsiTreeUtil

open class JsTypeDefStubBasedElementImpl<StubT : StubElement<out PsiElement>> : StubBasedPsiElementBase<StubT>, JsTypeDefElement, JsTypeDefStubBasedElement<StubT> {

    override val containerName: String?
        get() {
            val parentClass = getParentOfType(JsTypeDefInterfaceElement::class.java)
            if (parentClass != null) {
                val typeName = parentClass.typeName?.id?.text
                if (typeName != null)
                    return typeName
            }
            val module = getParentOfType(JsTypeDefModule::class.java)
            if (module != null) {
                return module.namespacedName
            }
            return containingFile.name
        }

    override val containingTypeDefFile: JsTypeDefFile
        get() {
            val file = containingFile
            assert(file is JsTypeDefFile) { "JsTypeDefElement is not inside a JsTypeDefFile: " + file + " " + if (file.isValid) file.text else "<invalid>" }
            return file as JsTypeDefFile
        }

    constructor(stub: StubT, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    override fun getLanguage(): Language {
        return JsTypeDefLanguage.instance
    }

    override fun toString(): String {
        return elementType.toString()
    }

    override fun <T:PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T:PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T:PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

}