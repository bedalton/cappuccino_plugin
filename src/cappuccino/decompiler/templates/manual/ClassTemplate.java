package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassTemplate implements TemplateElement {
    private String className = null;
    private String superClassOrCategoryName;
    private List<String> protocols = new ArrayList<>();
    private List<TemplateElement> bodyElements = new ArrayList<>();
    private Map<String, String> ivars = new HashMap<>();
    private ClassType classType;
    private int paddingBeforeIVar = 0;
    private static final int MIN_PADDING_IN_IVARS = 8;


    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuperClassOrCategoryName(String superClassOrCategoryName) {
        this.superClassOrCategoryName = superClassOrCategoryName;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public void addProtocol(final String protocol) {
        protocols.add(protocol);
    }

    public void addBodyElement(TemplateElement templateElement) {
        bodyElements.add(templateElement);
    }

    public void addIVar(@NotNull String varName, @NotNull String type) {
        ivars.put(varName, type);
        int varTypeLength = type.length() + MIN_PADDING_IN_IVARS;
        if (varTypeLength > paddingBeforeIVar) {
            paddingBeforeIVar = varTypeLength;
        }
    }

    @NotNull
    @Override
    public StringBuilder appendTo(@NotNull final StringBuilder stringBuilder) {
        stringBuilder.append(classType.header).append(" ");
        stringBuilder.append(className);
        switch(classType) {
            case CATEGORY:
                stringBuilder.append(" (").append(superClassOrCategoryName != null ? superClassOrCategoryName : "Undef").append(")");
                break;
            case IMPLEMENTATION:
                if (superClassOrCategoryName != null) {
                    stringBuilder.append(" : ").append(superClassOrCategoryName);
                }
                break;
        }
        if (!protocols.isEmpty()) {
            stringBuilder.append(" <");
            final int numProtocols = protocols.size();
            for (int i=0; i<numProtocols;i++) {
                stringBuilder.append(protocols.get(i));
                if (i < numProtocols-1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(">");
        }
        stringBuilder.append("\n");
        if (ivars.size() > 0) {
            stringBuilder.append("{\n");
            for (String varName : ivars.keySet()) {
                final String varType = ivars.get(varName);
                stringBuilder.append("    ")
                        .append(varType);
                int numSpaces = paddingBeforeIVar - varType.length();
                for (int i=0; i<numSpaces;i++) {
                    stringBuilder.append(" ");
                }
                stringBuilder
                    .append(varName)
                    .append("\n");
            }
            stringBuilder.append("}\n");
        }
        stringBuilder.append("\n");
        bodyElements.sort((var1,var2) -> {
            if (var1 instanceof MethodTemplate && var2 instanceof MethodTemplate) {
                MethodTemplate method1 = (MethodTemplate)var1;
                MethodTemplate method2 = (MethodTemplate)var2;
                if (method1.isStatic() && method2.isStatic()) {
                    return  method1.getSelector().compareToIgnoreCase(method2.getSelector());
                } else if (method1.isStatic()) {
                    return -1;
                } else if (method2.isStatic()){
                    return 1;
                }
                return 0;
            }
            return 0;
        });
        for (TemplateElement templateElement : bodyElements) {
            templateElement.appendTo(stringBuilder);
        }
        if (classType == ClassType.PROTOCOL) {
            stringBuilder.append("\n");
        }
        stringBuilder.append("@end\n\n");
        return stringBuilder;
    }

    public static enum ClassType {
        IMPLEMENTATION("@implementation"),
        PROTOCOL("@protocol"),
        CATEGORY("@implementation");
        final String header;
        ClassType(final String header) {
            this.header = header;
        }
    }
}
