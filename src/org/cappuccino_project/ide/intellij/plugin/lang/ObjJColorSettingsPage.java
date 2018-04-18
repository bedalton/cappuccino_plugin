package org.cappuccino_project.ide.intellij.plugin.lang;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class ObjJColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("@Directives", ObjJSyntaxHighlighter.AT_STATEMENT),
            new AttributesDescriptor("#Preprocessors", ObjJSyntaxHighlighter.PRE_PROCESSOR),
            //new AttributesDescriptor("Identifier", ObjectiveJSyntaxHighlighter.ID),
            new AttributesDescriptor("Keyword", ObjJSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("String", ObjJSyntaxHighlighter.STRING),
            new AttributesDescriptor("Line comment", ObjJSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Block comment", ObjJSyntaxHighlighter.BLOCK_COMMENT),
            new AttributesDescriptor("Secondary Literal", ObjJSyntaxHighlighter.SECONDARY_LITERAL)
    };

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ObjJIcons.DOCUMENT_ICON;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new ObjJSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return
                "@import <Framework/CPString.j>\n" +
                "/* block comment */\n"+
                "@implementation Person : CPObject\n" +
                "{\n" +
                "    CPString name;\n" +
                "}\n" +
                "- (void) setName\n" +
                "{\n" +
                "   // line comment\n"+
                "    var a = 10;\n" +
                "}\n" +
                "+ (CPString) getHello:(CPString) name\n" +
                "{\n" +
                "    return \"Hello\" + name;\n" +
                "}\n" +
                "\n" +
                "@end";
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "ObjectiveJ";
    }
}
