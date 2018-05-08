package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeReference
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeResults
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil

import java.util.ArrayList
import java.util.Collections
import java.util.logging.Level
import java.util.logging.Logger

import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.*

object ObjJMethodCallPsiUtil {

    private val LOGGER = Logger.getLogger(ObjJMethodCallPsiUtil::class.java.name)
    private val GET_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("class")
    private val GET_SUPERCLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("superclass")
    val IS_KIND_OF_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("isKindOfClass")

    fun getSelectorString(methodCall: ObjJMethodCall): String {
        val stub = methodCall.stub
        return stub?.selectorString
                ?: ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(methodCall.selectorStrings)
    }


    fun isUniversalMethodCaller(className: String): Boolean {
        return !isPrimitive(className) && (UNDETERMINED == className || UNDEF_CLASS_NAME == className) || ID == className
    }

    fun getSelectorStrings(methodCall: ObjJMethodCall): List<String> {
        return if (methodCall.stub != null && !methodCall.stub.selectorStrings.isEmpty()) {
            methodCall.stub.selectorStrings
        } else ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(getSelectorList(methodCall))
    }

    fun getSelectorList(methodCall: ObjJMethodCall): List<ObjJSelector> {
        if (methodCall.selector != null) {
            return listOf<ObjJSelector>(methodCall.selector)
        }
        val out = ArrayList<ObjJSelector>()
        for (qualifiedSelector in methodCall.qualifiedMethodCallSelectorList) {
            out.add(qualifiedSelector.selector)
        }
        return out
    }

    fun getCallTargetText(methodCall: ObjJMethodCall): String {
        return if (methodCall.stub != null) {
            methodCall.stub.callTarget
        } else methodCall.callTarget.text
    }

}
