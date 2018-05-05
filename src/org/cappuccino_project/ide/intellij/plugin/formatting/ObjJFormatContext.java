package org.cappuccino_project.ide.intellij.plugin.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ObjJFormatContext {
    private final CommonCodeStyleSettings commonSettings;
    private final ObjJCodeStyleSettings objJSettings;
    public final SpacingBuilder spacingBuilder;

    private Alignment alignment;

    private boolean metLeftBrace;

    public ObjJFormatContext(@NotNull final CommonCodeStyleSettings commonSettings, @NotNull final ObjJCodeStyleSettings objJSettings) {
        this.commonSettings = commonSettings;
        this.objJSettings = objJSettings;
        spacingBuilder = ObjJSpacingBuilder.createSpacingBuilder();
    }

    public Alignment alignment () {
        return alignment;
    }

    public ObjJFormatContext setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public boolean metLeftBrace() {
        return metLeftBrace;
    }

    public ObjJFormatContext setMetLeftBrace(boolean metLeftBrace) {
        this.metLeftBrace = metLeftBrace;
        return this;
    }


    public CommonCodeStyleSettings getCommonSettings() {
        return commonSettings;
    }

    public ObjJCodeStyleSettings getObjJSettings() {
        return objJSettings;
    }

}
