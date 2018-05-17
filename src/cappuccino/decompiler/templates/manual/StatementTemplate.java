package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;

public class StatementTemplate implements TemplateElement {
    private final String statement;
    public StatementTemplate(String statement) {
        this.statement = statement;
    }

    @NotNull
    @Override
    public StringBuilder appendTo(@NotNull final StringBuilder stringBuilder) {
        stringBuilder.append(statement);
        return stringBuilder;
    }
}
