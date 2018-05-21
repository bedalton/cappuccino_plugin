package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclarationSelector
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings

internal class ObjJSpacingBuilder(
        codeStyleSettings: CodeStyleSettings) : SpacingBuilder(codeStyleSettings, ObjJLanguage.instance) {

    override fun getSpacing(parent: Block?, child1: Block?, child2: Block?): Spacing? {
        if (parent is ASTBlock && child1 is ASTBlock && child2 is ASTBlock) {
            val calculator: SpacingCalculator = SpacingCalculator(parent, child1, child2)
            return calculator.getSpacing() ?: super.getSpacing(parent, child1, child2)
        }
        return super.getSpacing(parent, child1, child2)
    }


    private inner class SpacingCalculator(private val parent: ASTBlock, private val child1: ASTBlock, private val child2: ASTBlock) {

        val parentNode: ASTNode = parent.node

        val child1Node: ASTNode = child1.node

        val child2Node: ASTNode = child2.node

        val parentElement: PsiElement = parent.node.psi

        val child1Element: PsiElement = child1.node.psi

        val child2Element: PsiElement = child2.node.psi

        val document:Document? = PsiDocumentManager.getInstance(parentElement.project).getDocument(parentElement.containingFile)

        fun getSpacing() : Spacing? {
            return when (parentElement) {
                is ObjJBlock -> {
                    null
                }
                is ObjJMethodDeclaration -> {
                    getChildSelectorSpacing()
                }
                else -> null
            }
        }

        private fun getChildSelectorSpacing() : Spacing? {
            if (document == null || child1 !is ObjJMethodDeclarationSelector || child2 !is ObjJMethodDeclarationSelector) {
                return null
            }
            val child1LineNumber = document.getLineNumber(child1Node.startOffset)
            val child2LineNumber = document.getLineNumber(child2Node.startOffset)
            if (child1LineNumber == child2LineNumber) {
                return null
            }
            val spacesBeforeChild1Colon = child1.colon.textOffset - document.getLineEndOffset(child1LineNumber)
            val spacesBeforeChild2Colon = child2.colon.textOffset - document.getLineEndOffset(child1LineNumber)
            val spaces = spacesBeforeChild1Colon - spacesBeforeChild2Colon
            return Spacing.createSpacing(if (spaces > 0) spaces else  0, if (spaces > 0) spaces else Integer.MAX_VALUE, 1, true, 0)
        }
    }

}
