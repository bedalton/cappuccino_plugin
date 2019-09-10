package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.getFrameworkTextRangeInComment
import cappuccino.ide.intellij.plugin.psi.utils.getFileWithFramework
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.utils.isInvalid
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

class JsTypeDefCommentFileReference(comment:PsiComment, rangeInComment:TextRange) : PsiPolyVariantReferenceBase<PsiComment>(comment, rangeInComment) {

    val frameworkName:String? by lazy {
        getFrameworkNameInComment()
    }

    val fileName:String? by lazy {
        if (rangeInComment.startOffset < 0 || comment.textLength <= rangeInComment.endOffset)
            null
        else
            comment.text.substring(rangeInComment.startOffset, rangeInComment.endOffset)
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        if (DumbService.isDumb(myElement.project))
            return PsiElementResolveResult.EMPTY_ARRAY
        val fileName = fileName ?: return emptyArray()
        val frameworkName = frameworkName
        val files = getFileWithFramework(fileName, frameworkName, myElement.project)
        if (files.isEmpty())
            return PsiElementResolveResult.EMPTY_ARRAY
        return PsiElementResolveResult.createResults(files)
    }

    private fun getFrameworkNameInComment():String? {
        var frameworkName: String?
        var previousSibling:PsiElement? =  myElement
        while(previousSibling != null) {
            ProgressIndicatorProvider.checkCanceled()
            if (previousSibling is PsiComment) {
                frameworkName = getFrameworkInComment(previousSibling)
                if (frameworkName != null) {
                    return frameworkName
                }
            }
            previousSibling = previousSibling.getPreviousNonEmptySibling(true)
        }
        return null
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }


    private fun getFrameworkInComment(comment:PsiComment):String? {
        val range = comment.getFrameworkTextRangeInComment(false) ?: return null
        if (range.isInvalid()) {
            return null
        }
        if (range.endOffset >= comment.textLength) {
            return null
        }
        return comment.text.substring(range.startOffset, range.endOffset)
    }
}