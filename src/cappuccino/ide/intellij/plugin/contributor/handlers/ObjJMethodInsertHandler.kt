package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.utils.subList
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.EmptyNode
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.VariableNode

class ObjJMethodInsertHandler(private val selectorList:List<ObjJSelectorStruct>, private val spaceAfterSelector:Boolean) : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
        if (selectorList.size == 1 && !selectorList[0].hasColon) {
            return
        }
        val space = if (spaceAfterSelector) " " else ""
        //val tail = if (withSpace) " " else "" // Never needs tail as parameters are included
        val templateText = ":$space\$${selectorList[0].selector}\$ "+ selectorList.subList(1).joinToString(" ") { "${it.selector}:$space\$${it.selector}\$"}// + tail
        val template = TemplateImpl("", templateText, "")
        for(i in 0 ..selectorList.lastIndex) {
            val parameter = selectorList[i]
            template.addVariable(parameter.selector, VariableNode(parameter.selector, EmptyNode()), false)
        }
        val editor = context.editor
        TemplateManager.getInstance(context.project).startTemplate(editor, template)
    }
}