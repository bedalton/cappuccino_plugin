package cappuccino.ide.intellij.plugin.jstypedef.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVisitor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor

class JsTypeDefUnknownTypeInspection : LocalInspectionTool() {
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
        if (typeName != null && !classExists(type.project, typeName.text)) {
            problemsHolder.registerProblem(typeName, JsTypeDefBundle.message("jstypedef.inspections.invalid-type.error.message", typeName))
        }
    }

    private fun classExists(project:Project, typeName:String) : Boolean {
        return JsTypeDefClassesByNameIndex.instance.containsKey(typeName, project)
            || ObjJClassDeclarationsIndex.instance.containsKey(typeName, project)

    }
}