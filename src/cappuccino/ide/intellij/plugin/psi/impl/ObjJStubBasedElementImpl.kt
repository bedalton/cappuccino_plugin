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
package cappuccino.ide.intellij.plugin.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.*
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.ObjJElementUtils
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubElementType
import com.intellij.psi.util.PsiTreeUtil

import java.util.Arrays

open class ObjJStubBasedElementImpl<T : StubElement<out PsiElement>> : StubBasedPsiElementBase<T>, ObjJCompositeElement, ObjJStubBasedElement<T> {

    override val containingObjJFile: ObjJFile
        get() {
            val file = containingFile
            assert(file is ObjJFile) { "KtElement not inside KtFile: " + file + " " + if (file.isValid) file.text else "<invalid>" }
            return file as ObjJFile
        }

    val psiOrParent: ObjJCompositeElement
        get() = this

    constructor(stub: T, nodeType: IStubElementType<*, *>) : super(stub, nodeType) {}

    constructor(node: ASTNode) : super(node) {}

    override fun getLanguage(): Language {
        return ObjJLanguage.INSTANCE
    }

    override fun toString(): String {
        return elementType.toString()
    }

    @Throws(IncorrectOperationException::class)
    override fun delete() {
        ObjJElementUtils.deleteSemicolon(this)
        super.delete()
    }

    override fun <T:PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T:PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T:PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

}