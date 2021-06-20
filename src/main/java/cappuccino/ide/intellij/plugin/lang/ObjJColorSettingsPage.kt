package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icons.ObjJIcons
import javax.swing.Icon

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
@implementation MyClass : <<variableType>HasProtocol</variableType>>
{
    <variableType>id</variableType> _reference;
    <variableType>int</variableType> _variableInt;
    <variableType>CPString</variableType> _string  @accessors(property=string);
    <variableType>CPColor</variableType> _backgroundColor;
}

-(void) setBackgroundColor:(<variableType>CPColor</variableType>)aColor
{
    //A Line Comment
    var i = 0,
        regexp = <literal2>/[hH]ello/g</literal2>;
    <identifier>i</identifier>++
    <instanceVar>_backgroundColor</instanceVar> = <parameterVar>aColor</parameterVar>;
}

-(<variableType>CPString</variableType>) colorHex
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

// Global Function
<globalFunctionNames>thisFunctionIsGlobal</globalFunctionNames>(aVar)

//This is a global variable
globalGreeting = @"Hello %s!";

//This is a regular variable
var element = <js_var>document</js_var>.<funcName>getElementById</funcName>("tagName");

<fileLevelVariable>element</fileLevelVariable>.<identifier>innerHTML</identifier> = @"21";

var isValidName = function(aName)
{
    if (<parameterVar>aName</parameterVar>)
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
    var fab = -<js_val>SQRT</js_val>,
        abFab = <js_func>abs(fab)</js_func>;

    if (<funcName>isValidName</funcName>(<parameterVar>aName</parameterVar>))
    {
        return [<variableType>CPString</variableType> stringWithFormat:<globalVar>globalGreeting</globalVar>, <parameterVar>aName</parameterVar>];
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
        private val DESCRIPTORS = arrayOf(
                AttributesDescriptor("@Directives", ObjJSyntaxHighlighter.AT_STATEMENT),
                AttributesDescriptor("#Preprocessors", ObjJSyntaxHighlighter.PRE_PROCESSOR),
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
                AttributesDescriptor("Global Function Calls", ObjJSyntaxHighlighter.GLOBAL_FUNCTION_NAME),
                AttributesDescriptor("Global Javascript Function Calls", ObjJSyntaxHighlighter.JS_TYPEDEF_FUNCTION_NAME),
                AttributesDescriptor("File Level Variables", ObjJSyntaxHighlighter.FILE_LEVEL_VARIABLE),
                AttributesDescriptor("Global Variables", ObjJSyntaxHighlighter.GLOBAL_VARIABLE),
                AttributesDescriptor("Global Javascript Variables", ObjJSyntaxHighlighter.JS_TYPEDEF_VARIABLE),
                AttributesDescriptor("Global Javascript Functions", ObjJSyntaxHighlighter.JS_TYPEDEF_FUNCTION_NAME)
        )
        private val XMLDESCRIPTORS: HashMap<String, TextAttributesKey> = hashMapOf(
                "identifier" to ObjJSyntaxHighlighter.ID,
                "keyword" to ObjJSyntaxHighlighter.KEYWORD,
                "string" to ObjJSyntaxHighlighter.STRING,
                "literal2" to ObjJSyntaxHighlighter.SECONDARY_LITERAL,
                "variableType" to ObjJSyntaxHighlighter.VARIABLE_TYPE,
                "fileLevelVariable" to ObjJSyntaxHighlighter.FILE_LEVEL_VARIABLE,
                "instanceVar" to ObjJSyntaxHighlighter.INSTANCE_VARIABLE,
                "parameterVar" to ObjJSyntaxHighlighter.PARAMETER_VARIABLE,
                "globalVar" to ObjJSyntaxHighlighter.GLOBAL_VARIABLE,
                "functionNames" to ObjJSyntaxHighlighter.FUNCTION_NAME,
                "globalFunctionNames" to ObjJSyntaxHighlighter.GLOBAL_FUNCTION_NAME,
                "pp" to ObjJSyntaxHighlighter.PRE_PROCESSOR,
                "at" to ObjJSyntaxHighlighter.AT_STATEMENT,
                "funcName" to ObjJSyntaxHighlighter.FUNCTION_NAME,
                "js_var" to ObjJSyntaxHighlighter.JS_TYPEDEF_VARIABLE,
                "js_func" to ObjJSyntaxHighlighter.JS_TYPEDEF_FUNCTION_NAME

        )
    }
}
