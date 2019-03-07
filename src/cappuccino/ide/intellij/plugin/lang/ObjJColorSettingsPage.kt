package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icons.ObjJIcons

import javax.swing.*

class ObjJColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon? {
        return ObjJIcons.DOCUMENT_ICON
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return ObjJSyntaxHighlighter()
    }

    override fun getDemoText(): String {
        return """
@import <Foundation/Foundation.j>
#include "SomeClass.j"

/*
    A Block Comment
*/
@implementation MyClass : <<varType>HasProtocol</varType>>
{
    <varType>id</varType> _reference;
    <varType>int</varType> _varInt;
    <varType>CPString</varType> _string  @accessors(property=string);
    <varType>CPColor</varType> _backgroundColor;
}

-(void) setBackgroundColor:(<varType>CPColor</varType>)aColor
{
    //A Line Comment
    var i = 0;

    <identifier>i</identifier>++
    <instanceVar>_backgroundColor</instanceVar> = <paramVar>aColor</paramVar>;
}

-(<varType>CPString</varType>) colorHex
{
    if (<instanceVar>_backgroundColor</instanceVar>)
    {
        return [<instanceVar>_backgroundColor</instanceVar> hexString];
    }
    else
    {
        return Nil
    }
}

@end

#pragma mark - Greeting Functions

//This is a global variable
globalGreeting = @"Hello %s!";

//This is a regular variable
var element = document.<funcName>getElementById</funcName>("tagName");

<identifier>element</identifier>.<identifier>innerHTML</identifier> = @"21";

var isValidName = function(aName)
{
    if (<paramVar>aName</paramVar>)
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

function sayHello(aName)
{
    if (<funcName>isValidName</funcName>(<paramVar>aName</paramVar>))
    {
        return [<varType>CPString</varType> stringWithFormat:<globalVar>globalGreeting</globalVar>, <paramVar>aName</paramVar>];
    }
    return null;
}

        """
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return DESCRIPTORS
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String {
        return "Objective-J"
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
        return XMLDESCRIPTORS
    }

    companion object {
        private val DESCRIPTORS = arrayOf(AttributesDescriptor("@Directives", ObjJSyntaxHighlighter.AT_STATEMENT), AttributesDescriptor("#Preprocessors", ObjJSyntaxHighlighter.PRE_PROCESSOR),
                AttributesDescriptor("Identifier", ObjJSyntaxHighlighter.ID),
                AttributesDescriptor("Keyword", ObjJSyntaxHighlighter.KEYWORD),
                AttributesDescriptor("String", ObjJSyntaxHighlighter.STRING),
                AttributesDescriptor("Line comment", ObjJSyntaxHighlighter.LINE_COMMENT),
                AttributesDescriptor("Block comment", ObjJSyntaxHighlighter.BLOCK_COMMENT),
                AttributesDescriptor("Preprocessor Keywords", ObjJSyntaxHighlighter.PRE_PROCESSOR),
                AttributesDescriptor("Objective-J Keywords", ObjJSyntaxHighlighter.AT_STATEMENT),
                AttributesDescriptor("Secondary Literal", ObjJSyntaxHighlighter.SECONDARY_LITERAL),
                AttributesDescriptor("Class Types", ObjJSyntaxHighlighter.VARIABLE_TYPE),
                AttributesDescriptor("Instance variables", ObjJSyntaxHighlighter.INSTANCE_VARIABLE),
                AttributesDescriptor("Parameter Variables", ObjJSyntaxHighlighter.PARAMETER_VARIABLE),
                AttributesDescriptor("Function Calls", ObjJSyntaxHighlighter.FUNCTION_NAME),
                AttributesDescriptor("File Level Variables", ObjJSyntaxHighlighter.FILE_LEVEL_VARIABLE),
                AttributesDescriptor("Global Variables", ObjJSyntaxHighlighter.GLOBAL_VARIABLE))
        private val XMLDESCRIPTORS: HashMap<String, TextAttributesKey> = hashMapOf(
                "identifier" to ObjJSyntaxHighlighter.ID,
                "keyword" to ObjJSyntaxHighlighter.KEYWORD,
                "string" to ObjJSyntaxHighlighter.STRING,
                "literal2" to ObjJSyntaxHighlighter.SECONDARY_LITERAL,
                "varType" to ObjJSyntaxHighlighter.VARIABLE_TYPE,
                "instanceVar" to ObjJSyntaxHighlighter.INSTANCE_VARIABLE,
                "paramVar" to ObjJSyntaxHighlighter.PARAMETER_VARIABLE,
                "globalVar" to ObjJSyntaxHighlighter.GLOBAL_VARIABLE,
                "functionNames" to ObjJSyntaxHighlighter.FUNCTION_NAME,
                "pp" to ObjJSyntaxHighlighter.PRE_PROCESSOR,
                "at" to ObjJSyntaxHighlighter.AT_STATEMENT,
                "funcName" to ObjJSyntaxHighlighter.FUNCTION_NAME
        )
    }
}
