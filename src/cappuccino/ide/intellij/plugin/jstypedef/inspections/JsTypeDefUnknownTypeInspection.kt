package cappuccino.ide.intellij.plugin.jstypedef.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsPrimitives
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefKeyListsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefArrayType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefExtendsStatement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVisitor
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasGenerics
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
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
            override fun visitType(type: JsTypeDefType) {
                super.visitType(type)
                annotateTypeIfNecessary(type, problemsHolder)
            }
        }
    }

    private fun annotateTypeIfNecessary(type:JsTypeDefType, problemsHolder: ProblemsHolder) {
        val typeName = type.typeName
        if (type.parent !is JsTypeDefExtendsStatement && type.parent !is JsTypeDefClassDeclaration<*,*> && type.parent !is JsTypeDefHasGenerics && type.parent !is JsTypeDefArrayType &&  typeName != null && !classExists(type.project, typeName.text) && typeName.text !in type.enclosingGenerics) {
            LOGGER.warning("JsTypeDef type does not exist")
            problemsHolder.registerProblem(typeName, JsTypeDefBundle.message("jstypedef.inspections.invalid-type.error.message", typeName.text))
        }
    }

    private fun classExists(project:Project, typeName:String) : Boolean {
        return JsTypeDefClassesByNameIndex.instance.containsKey(typeName, project)
            || ObjJClassDeclarationsIndex.instance.containsKey(typeName, project)
                ||  JsTypeDefKeyListsByNameIndex.instance.containsKey(typeName, project)
                || JsPrimitives.isPrimitive(typeName)

    }

    companion object {
        val LOGGER:Logger by lazy {
            Logger.getLogger("#${JsTypeDefUnknownTypeInspection::class.java.name}")
        }
    }
}