package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.getNextSiblingOfType
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

import java.util.ArrayList

class ObjJCodeFoldingBuilder : FoldingBuilderEx() {
    val foldingDescriptors = ArrayList<FoldingDescriptor>()
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("objj")
        PsiTreeUtil.processElements(root,
                { element ->
                    return@processElements when (element) {
                        is ObjJImplementationDeclaration -> execute(element)
                        is ObjJProtocolDeclaration -> execute(element)
                        //is ObjJBodyVariableAssignment -> execute(element)
                        is ObjJPreprocessorPragma -> execute(element)
                        is ObjJMethodDeclaration -> execute(element)
                        is ObjJFunctionDeclaration -> execute(element)
                        is ObjJInstanceVariableList -> execute(element)
                        //is ObjJComment -> execute(element)
                        else -> true
                    }
                })
        return foldingDescriptors.toTypedArray()
    }


    fun execute(declaration: ObjJImplementationDeclaration): Boolean {
        val startElement = (declaration.inheritedProtocolList ?: declaration.categoryName?: declaration.superClass ?: declaration.getClassName() ?: return true)
        val startOffset:Int = startElement.textRange.endOffset
        val endOffset:Int = declaration.atEnd?.textRange?.endOffset ?: declaration.textRange.endOffset
        if (startOffset <= endOffset) {
            foldingDescriptors.add(FoldingDescriptor(startElement, TextRange(startOffset, endOffset)))
        }
        return true
    }

    fun execute(protocol:ObjJProtocolDeclaration) : Boolean {
        val startOffset = (protocol.inheritedProtocolList ?: protocol.getClassName() ?: return true).textRange.endOffset
        val endOffset = (protocol.atEnd?.textRange?.endOffset ?: protocol.textRange.endOffset) - 1
        if (startOffset < endOffset) {
            foldingDescriptors.add(FoldingDescriptor(protocol, TextRange(startOffset, endOffset)))
        }
        return true
    }

    fun execute(pragma:ObjJPreprocessorPragma) : Boolean {
        val startOffset:Int = pragma.textRange.endOffset
        val endOffset:Int = pragma.getNextSiblingOfType(ObjJPreprocessorPragma::class.java)?.textRange?.endOffset ?: pragma.containingFile.textRange.endOffset
        if (startOffset < endOffset) {
            foldingDescriptors.add(FoldingDescriptor(pragma.node, TextRange(startOffset, endOffset)))
        }
        return true
    }

    fun execute(variableAssignment: ObjJBodyVariableAssignment) : Boolean {
        val startOffset = (variableAssignment.varModifier?.textRange?.endOffset ?: return true) + 1
        val endOffset = variableAssignment.textRange.endOffset - 1
        if (startOffset>= endOffset) {
            return true
        }
        foldingDescriptors.add(FoldingDescriptor(variableAssignment.node, TextRange(startOffset, endOffset)))
        return true
    }

    fun execute(function: ObjJFunctionDeclaration) : Boolean {
        val startOffset = function.functionName?.textRange?.endOffset ?: return true
        val endOffset = (function.block?.textRange?.endOffset ?: return true)
        foldingDescriptors.add(FoldingDescriptor(function.node, TextRange(startOffset, endOffset)))
        return true
    }

    fun execute(methodDeclaration:ObjJMethodDeclaration) : Boolean {
        val startRange = methodDeclaration.methodHeader.textRange.endOffset
        val endRange = (methodDeclaration.block?.textRange?.endOffset ?: return true)
        foldingDescriptors.add(FoldingDescriptor(methodDeclaration.node, TextRange(startRange, endRange)))
        return true
    }

    fun execute(instanceVariablesList:ObjJInstanceVariableList) : Boolean {
        val endOffset: Int = instanceVariablesList.textRange.endOffset - 1
        val startOffset = if (instanceVariablesList.textRange.startOffset + 1 < endOffset)
            instanceVariablesList.textRange.startOffset// + 1
        else
            instanceVariablesList.textRange.startOffset
        foldingDescriptors.add(FoldingDescriptor(instanceVariablesList.node, TextRange(startOffset, endOffset)))
        return true
    }

    fun execute(comment:ObjJComment) : Boolean {
        val startRange = comment.textRange.startOffset + 2
        val endRange:Int = comment.textRange.endOffset - 2
        foldingDescriptors.add(FoldingDescriptor(comment.node, TextRange(startRange, endRange)))
        return true
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}