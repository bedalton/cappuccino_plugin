package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FunctionTemplate implements TemplateElement {
    private String functionName;
    private List<String> paramNames;

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void addParam(@NotNull String paramName) {
        if (paramNames == null) {
             paramNames = new ArrayList<>();
        }
        if (paramName.isEmpty()) {
            paramName = "var"+(paramNames.size()+1);
        }
        paramNames.add(paramName);
    }

    private StringBuilder appendParamNames(@NotNull final StringBuilder stringBuilder) {
        if (paramNames == null) {
            return stringBuilder;
        }
        int numParams = paramNames.size();
        int lastIndex = numParams - 1;
        for (int i=0;i<numParams;i++) {
            stringBuilder.append(paramNames.get(i));
            if (i != lastIndex) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder;
    }

    @NotNull
    @Override
    public StringBuilder appendTo(
            @NotNull final
                    StringBuilder stringBuilder) {
        stringBuilder.append("function");
        if (functionName != null) {
            stringBuilder.append(" ").append(functionName);
        }
        stringBuilder.append("(");
        appendParamNames(stringBuilder)
            .append(") {\n")
            .append("    /* compiled code */\n")
            .append("}");
        if (functionName != null) {
            stringBuilder.append("\n\n");
        }
        return stringBuilder;
    }
}
