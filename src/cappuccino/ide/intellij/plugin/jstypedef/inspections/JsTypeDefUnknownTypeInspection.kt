package cappuccino.ide.intellij.plugin.jstypedef.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJTypeDefIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsPrimitives
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByPartialNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefKeyListsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasGenerics
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import java.util.logging.Logger

class JsTypeDefUnknownTypeInspection : LocalInspectionTool() {

    override fun getGroupDisplayName(): String {
        return JsTypeDefBundle.message("jstypedef.inspections.group-name")
    }

    override fun getShortName(): String {
        return JsTypeDefBundle.message("jstypedef.inspections.invalid-type.shortName")
    }

    override fun getDisplayName(): String {
        return JsTypeDefBundle.message("jstypedef.inspections.invalid-type.display-name")
    }

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JsTypeDefVisitor() {
            override fun visitTypeName(typeName: JsTypeDefTypeName) {
                super.visitTypeName(typeName)
                annotateTypeIfNecessary(typeName, problemsHolder)
            }
        }
    }

    private fun annotateTypeIfNecessary(typeName: JsTypeDefTypeName, problemsHolder: ProblemsHolder) {
        if (ignoreTypeName(typeName))
            return
        val project = typeName.project
        val typeNameString = typeName.text
        val namespaceComponents = typeName.previousSiblings + typeName
        val namespacedName = if (typeName.parent is JsTypeDefQualifiedTypeName) namespaceComponents.joinToString(".") { it.text } else null
        if (namespacedName.isNotNullOrBlank()) {
            if (JsTypeDefClassesByPartialNamespaceIndex.instance.containsKey(namespacedName!!, project)) {
                return
            }
            problemsHolder.registerProblem(typeName, JsTypeDefBundle.message("jstypedef.inspections.invalid-type.error.message", namespacedName))
            return
        }
        // Type is existing class
        if (classExists(project, typeNameString))
            return
        // Type References generic type
        if (typeNameString in (typeName.parent as? JsTypeDefType)?.enclosingGenerics.orEmpty()) {
            return
        }

        // Type is itself a generic type
        if (typeIsGenericTypeDeclaration(typeName)) {
            return
        }
        problemsHolder.registerProblem(typeName, JsTypeDefBundle.message("jstypedef.inspections.invalid-type.error.message", typeNameString))
    }

    private fun typeIsGenericTypeDeclaration(typeName: JsTypeDefTypeName): Boolean {
        val typesParent = typeName.getParentOfType(JsTypeDefGenericTypeTypes::class.java)?.parent
        return typesParent != null
                && (typesParent is JsTypeDefClassDeclaration<*, *> || typesParent is JsTypeDefFunction)
                && typeName.getPreviousNonEmptyNode(true)?.elementType != JsTypeDefTypes.JS_COLON
    }

    private fun ignoreTypeName(typeName: JsTypeDefTypeName): Boolean {
        val type = typeName.parent as? JsTypeDefType ?: return false
        return type.parent !is JsTypeDefExtendsStatement
                && type.parent !is JsTypeDefClassDeclaration<*, *>
                && type.parent !is JsTypeDefHasGenerics
                && type.parent !is JsTypeDefArrayType
    }

    private fun classExists(project: Project, typeName: String): Boolean {
        return JsTypeDefClassesByNameIndex.instance.containsKey(typeName, project)
                || JsTypeDefTypeAliasIndex.instance.containsKey(typeName, project)
                || ObjJClassDeclarationsIndex.instance.containsKey(typeName, project)
                || JsTypeDefKeyListsByNameIndex.instance.containsKey(typeName, project)
                || JsPrimitives.isPrimitive(typeName)
                || ObjJTypeDefIndex.instance.containsKey(typeName, project)

    }


    companion object {
        val LOGGER: Logger by lazy {
            Logger.getLogger("#${JsTypeDefUnknownTypeInspection::class.java.name}")
        }
    }
}