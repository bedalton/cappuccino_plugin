package cappuccino.ide.intellij.plugin.comments.inspections

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentTagLine
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentVisitor
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

class ObjJDocCommentBorrowTagValidator : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object: ObjJDocCommentVisitor() {
            override fun visitTagLine(o: ObjJDocCommentTagLine) {
                super.visitTagLine(o)
                validate(o, holder)
            }
        }
    }


    private fun validate(tagLine: ObjJDocCommentTagLine, problemsHolder: ProblemsHolder) {
        if (tagLine.tag != ObjJDocCommentKnownTag.BORROWS)
            return
        if (tagLine.asLiteral == null || tagLine.borrowedAs == null || tagLine.borrowedThat == null) {
            problemsHolder.registerProblem(tagLine, ObjJBundle.message("objj.comment.borrows.invalid"))
        }
    }

}



private fun