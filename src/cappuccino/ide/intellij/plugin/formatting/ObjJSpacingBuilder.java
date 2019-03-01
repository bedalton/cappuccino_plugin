package cappuccino.ide.intellij.plugin.formatting;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

class ObjJSpacingBuilder extends SpacingBuilder {
    ObjJSpacingBuilder(
            @NotNull
                    CodeStyleSettings codeStyleSettings) {
        super(codeStyleSettings, ObjJLanguage.getInstance());
    }
}
