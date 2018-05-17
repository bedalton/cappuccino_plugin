package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;

public class VariableDeclarationTemplate implements cappuccino.decompiler.templates.manual.TemplateElement {
    private String varName = null;
    private cappuccino.decompiler.templates.manual.TemplateElement body;

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public void setBody(TemplateElement body) {
        this.body = body;
    }

    private void assignBody(@NotNull final StringBuilder stringBuilder) {
        if (body == null) {
           stringBuilder.append(" = __COMPILED_CODE__");
        } else {
            stringBuilder.append(" = ");
            body.appendTo(stringBuilder);
        }
    }

    @NotNull
    @Override
    public StringBuilder appendTo(@NotNull final StringBuilder stringBuilder) {
        assert varName != null : "Variable name cannot be null";
        stringBuilder.append(varName);
        assignBody(stringBuilder);
        return stringBuilder;
    }
}
