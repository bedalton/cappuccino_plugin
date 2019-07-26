package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class ObjJLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage(): Language {
        return ObjJLanguage.instance
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_AFTER_COMMA")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE", "Space Between Selector and Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME", "Space After Variable Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR", "Space After Return Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_METHOD_TYPE_AND_RETURN_TYPE", "Space Between Method Type and Return Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "ALIGN_SELECTORS_IN_METHOD_DECLARATION", "Align Method Declaration Selector Colons", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL", "Space Between Selector and Value", "Method Call")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "ALIGN_SELECTORS_IN_METHOD_CALL", "Align Method Call Selector Colons", "Method Call")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "ALIGN_FIRST_SELECTOR_IN_METHOD_CALL", "Align First Method Call Selector", "Method Call")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BEFORE_PAREN_STATEMENT", "Space Between control keyword and paren statement", "General")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "ALIGN_PROPERTIES", "Align object properties colons", "General")
            consumer.showStandardOptions(
                    "SPACE_BEFORE_METHOD_CALL_PARENTHESES",
                    "SPACE_BEFORE_METHOD_PARENTHESES",
                    "SPACE_BEFORE_IF_PARENTHESES",
                    "SPACE_BEFORE_WHILE_PARENTHESES",
                    "SPACE_BEFORE_FOR_PARENTHESES",
                    "SPACE_BEFORE_CATCH_PARENTHESES",
                    "SPACE_BEFORE_SWITCH_PARENTHESES",
                    "SPACE_BEFORE_METHOD_LBRACE",
                    "SPACE_BEFORE_IF_LBRACE",
                    "SPACE_BEFORE_ELSE_LBRACE",
                    "SPACE_BEFORE_WHILE_LBRACE",
                    "SPACE_BEFORE_FOR_LBRACE",
                    "SPACE_BEFORE_DO_LBRACE",
                    "SPACE_BEFORE_SWITCH_LBRACE",
                    "SPACE_BEFORE_TRY_LBRACE",
                    "SPACE_BEFORE_CATCH_LBRACE",
                    "SPACE_BEFORE_FINALLY_LBRACE",
                    "SPACE_BEFORE_CATCH_KEYWORD"
            )
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions(
                    "IF_BRACE_FORCE",
                    "ELSE_ON_NEW_LINE",
                    "FOR_BRACE_FORCE",
                    "WHILE_BRACE_FORCE",
                    "DOWHILE_BRACE_FORCE",
                    "WHILE_ON_NEW_LINE",
                    "CATCH_ON_NEW_LINE",
                    "FINALLY_ON_NEW_LINE"
                    )
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "INSTANCE_VARIABLE_LIST_BRACE_FORCE", "Force Braces", "instance variable list", CodeStyleSettingsCustomizable.OptionAnchor.AFTER, "instance variable list", CodeStyleSettingsCustomizable.BRACE_OPTIONS, CodeStyleSettingsCustomizable.BRACE_VALUES)
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "FUNCTION_BRACE_FORCE", "Force Braces", "'function' statement",CodeStyleSettingsCustomizable.OptionAnchor.AFTER, "'function' statement", CodeStyleSettingsCustomizable.BRACE_OPTIONS, CodeStyleSettingsCustomizable.BRACE_VALUES)
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "TRY_ON_NEW_LINE", "'try' on new line", "'try' statement", CodeStyleSettingsCustomizable.OptionAnchor.BEFORE, "'try' statement")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "CATCH_BRACE_FORCE", "'catch' Force Braces", "'try' statement",CodeStyleSettingsCustomizable.OptionAnchor.AFTER, "'try' statement", CodeStyleSettingsCustomizable.BRACE_OPTIONS, CodeStyleSettingsCustomizable.BRACE_VALUES)
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "FINALLY_BRACE_FORCE", "'finally' Force Braces", "'try' statement",CodeStyleSettingsCustomizable.OptionAnchor.AFTER, "'try' statement", CodeStyleSettingsCustomizable.BRACE_OPTIONS, CodeStyleSettingsCustomizable.BRACE_VALUES)
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SWITCH_BRACE_FORCE", "Force Braces", "'switch' statement",CodeStyleSettingsCustomizable.OptionAnchor.AFTER, "'switch' statement", CodeStyleSettingsCustomizable.BRACE_OPTIONS, CodeStyleSettingsCustomizable.BRACE_VALUES)
        } else if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showAllStandardOptions()
        }
    }

    override fun getCodeSample(settingsType: SettingsType): String? {
    return """
@import <Foundation/Foundation.j>

#include "SomeClass.j"


/*
    A Block Comment
*/
@implementation MyClass : SuperClass <HasProtocol>{
id _reference;
int _varInt;
CPString _string  @accessors(property=string);
CPColor _backgroundColor;
}

-(void) setBackgroundColor:(CPColor)aColor{
    //A Line Comment
var i = 0;
i++;
_backgroundColor = aColor;
}

-(void) setBackgroundColor:(CPColor)aColor
forType:(DomType)clazz
withFilter:(Function)type{
    //A Line Comment
var i = 0;
i++;
_backgroundColor = aColor;
}

-(CPString)colorHex{
    if(_backgroundColor){
        return [_backgroundColor hexString];
    }
else{
return Nil
}
}

-(void)method2{
[self setBackgroundColor:_backgroundColor
forType:[DomType anchor]
withFilter: _domFilterFunction];
}


@end

#pragma mark - Greeting Functions

//This is a global variable
globalGreeting = @"Hello %s!";

//This is a regular variable
var element=document.getElementById("tagName");

element.innerHTML=@"21";

var isValidName = function(aName){
    if(!aName){
        return NO;
    }else if(aName.length > 0){
        return YES;
    }else{
        return NO;
    }
}

function sayHello(aName,var2){
    while(isValidName(aName,var2)){
        return [CPString stringWithFormat:globalGreeting,aName,var2];
    }
    return null;
}

do{
i++;
}while(i < 10);

try{
throw new Error();
}catch(e){
console.log(e);
}finally{
// ignore
}

""".trimIndent()
    }
}
