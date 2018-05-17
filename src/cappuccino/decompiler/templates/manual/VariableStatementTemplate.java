package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VariableStatementTemplate implements cappuccino.decompiler.templates.manual.TemplateElement {
    private String modifier = "var";
    private List<cappuccino.decompiler.templates.manual.TemplateElement> assignments = new ArrayList<>();

    public void addAssignments(TemplateElement templateElement) {
        this.assignments.add(templateElement);
    }

    public void setModifier(@NotNull String modifier) {
        this.modifier = modifier;
    }

    @NotNull
    @Override
    public StringBuilder appendTo(
            @NotNull final
                    StringBuilder stringBuilder) {
        if (assignments.isEmpty()) {
            return stringBuilder;
        }
        final int numAssignments = assignments.size();
        assert modifier != null;
        stringBuilder.append(modifier).append(" ");
        for (int i=0;i<numAssignments;i++) {
            if (i != 0) {
                stringBuilder.append("    ");
            }
            assignments.get(i).appendTo(stringBuilder);
            if (i != numAssignments-1) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append(";");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }
}
