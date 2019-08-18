package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import javafx.scene.control.ProgressIndicator

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
        var frameworkName:String? = null
        var previousSibling:PsiElement? =  myElement.getPreviousNonEmptySibling(true)
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


    private fun getFrameworkInComment(comment:PsiComment):String? {
        val atFramework = "@framework:"
        val text = comment.text
        val parts = text.split(atFramework)
        if (parts.size < 2)
            return null
        val after = parts[0].length + atFramework.length
        val frameworkPart = parts[1].trim()
        val frameworkMatch = "^([ -]*[a-zA-Z0-9_+.])+".toRegex().find(frameworkPart) ?: return null
        val frameworkName = frameworkMatch.value
        if (frameworkName.isBlank())
            return null
        LOGGER.info("FrameworkNameIsNotNullInJsTypeDefComment: $frameworkName")
        return frameworkName
    }


}