package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

import javax.swing.*

class ObjJColorSettingsPage : ColorSettingsPage {

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
        return null
    }

    override fun getIcon(): Icon? {
        return ObjJIcons.DOCUMENT_ICON
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return ObjJSyntaxHighlighter()
    }

    override fun getDemoText(): String {
        return "@import <Framework/CPString.j>\n" +
                "/* block comment */\n" +
                "@implementation Person : CPObject\n" +
                "{\n" +
                "    CPString name;\n" +
                "}\n" +
                "- (void) setName\n" +
                "{\n" +
                "   // line comment\n" +
                "    var a = 10;\n" +
                "}\n" +
                "+ (CPString) getHello:(CPString) name\n" +
                "{\n" +
                "    return \"Hello\" + name;\n" +
                "}\n" +
                "\n" +
                "@end"
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return DESCRIPTORS
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String {
        return "ObjectiveJ"
    }

    companion object {
        private val DESCRIPTORS = arrayOf(AttributesDescriptor("@Directives", ObjJSyntaxHighlighter.AT_STATEMENT), AttributesDescriptor("#Preprocessors", ObjJSyntaxHighlighter.PRE_PROCESSOR),
                //new AttributesDescriptor("Identifier", ObjectiveJSyntaxHighlighter.ID),
                AttributesDescriptor("Keyword", ObjJSyntaxHighlighter.KEYWORD), AttributesDescriptor("String", ObjJSyntaxHighlighter.STRING), AttributesDescriptor("Line comment", ObjJSyntaxHighlighter.LINE_COMMENT), AttributesDescriptor("Block comment", ObjJSyntaxHighlighter.BLOCK_COMMENT), AttributesDescriptor("Secondary Literal", ObjJSyntaxHighlighter.SECONDARY_LITERAL))
    }
}
