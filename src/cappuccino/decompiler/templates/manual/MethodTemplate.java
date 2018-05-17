package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MethodTemplate implements TemplateElement {

    private StringBuilder stringBuilder;
    private List<String> selectors;
    private List<String> paramTypes = new ArrayList<>();
    private List<String> variableNames = new ArrayList<>();
    private Boolean isStatic = null;
    private String returnType = null;
    private Boolean isAbstract = null;
    private String selector;

    public MethodTemplate() {
    }

    public void isStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isStatic() {
        assert isStatic != null;
        return isStatic;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selectorString) {
        selector = selectorString;
        String delim = "<>";
        selectorString = selectorString.replace(":", delim+":");
        String[] selectorArray = selectorString.split(":");
        selectors = new ArrayList<>();
        for (String selector : selectorArray) {
            selectors.add(selector.replace(delim, ""));
        }
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void addParamType(@Nullable
                                     String paramType) {
        paramTypes.add(paramType);
    }

    public void addVariableName(@Nullable
                                     String variableName) {
        variableNames.add(variableName);
    }

    public void setAbstract(Boolean anAbstract) {
        isAbstract = anAbstract;
    }

    @NotNull
    @Override
    public StringBuilder appendTo(@NotNull final StringBuilder stringBuilder) {
        assert (variableNames.size() == selectors.size()) && selectors.size() > 1 : "Invalid method parsed";
        assert isStatic != null : "Method must be marked as either static or non-static";
        assert returnType != null : "Return type must be set";
        assert isAbstract != null : "IsAbstract Must Be Set";

        stringBuilder.append(isStatic ? "+ " : "- ");
        stringBuilder.append("(").append(returnType).append(")");
        int numSelectors = selectors.size();
        for (int i=0; i<numSelectors;i++) {
            String selector = selectors.get(i);
            stringBuilder.append(selector);
            if (numSelectors > 1 || paramTypes.size() > 0) {
                stringBuilder.append(":");
            }
            if (i < paramTypes.size()) {
                stringBuilder.append("(").append(paramTypes.get(i)).append(")");
            }
            if (i < variableNames.size()) {
                stringBuilder.append(variableNames.get(i));
            } else if (paramTypes.size() > 0){
                stringBuilder.append("var").append(i+1);
            }
            if (i < numSelectors-1) {
                stringBuilder.append(" ");
            }
        }
        if (isAbstract) {
            stringBuilder.append(";\n");
        } else {
            stringBuilder.append("\n{\n    /* Compiled Code */\n}\n\n");
        }
        return stringBuilder;
    }
}
