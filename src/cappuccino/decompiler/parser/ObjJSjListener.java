package cappuccino.decompiler.parser;

import cappuccino.decompiler.templates.manual.*;
import cappuccino.decompiler.templates.manual.ClassTemplate.ClassType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cappuccino.decompiler.parser.ObjJSjParser.*;

public class ObjJSjListener extends ObjJSjParserBaseListener {

    private static final Logger LOGGER = Logger.getLogger(ObjJSjListener.class.getCanonicalName());
    private List<FileTemplate> fileTemplates = new ArrayList<>();
    private FileTemplate fileTemplate;
    private ClassTemplate classTemplate;
    private MethodTemplate methodTemplate;
    private int skipExpressions = 0;
    private Boolean isStaticMethod = null;
    private static final Map<String, Integer> numCategories = new HashMap<>();
    private VariableDeclarationTemplate variableDeclarationTemplate;
    private VariableStatementTemplate variableStatementTemplate;
    private static final Pattern IMPORT_PATTERN = Pattern.compile("[iI];\\d+;([^.]+\\.j)");

    public List<FileTemplate> getFileTemplates() {
        return fileTemplates;
    }

    @Override
    public void enterFile(FileContext ctx) {
        fileTemplate = new FileTemplate();
        fileTemplates.add(fileTemplate);
    }

    @Override
    public void exitFile(FileContext ctx) {
        fileTemplate = null;
    }

    @Override
    public void enterFileHeader(FileHeaderContext ctx) {
        String[] parts = ctx.fileNameProp().getText().split(";");
        fileTemplate.setFileName(parts[parts.length-1]);
    }

    @Override
    public void enterClassDefinition(ClassDefinitionContext ctx) {
        assert classTemplate == null;
        skipExpressions++;
        classTemplate = new ClassTemplate();
        classTemplate.setClassType(ClassType.IMPLEMENTATION);
        fileTemplate.addBodyElement(classTemplate);
        classTemplate.setClassName(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.className));
        String superClass = cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.superClass);
        if (superClass != null && !superClass.toLowerCase().equals("nil")) {
            classTemplate.setSuperClassOrCategoryName(superClass);
        }
    }

    @Override
    public void exitClassDefinition(ClassDefinitionContext ctx) {
        classTemplate = null;
        skipExpressions--;
    }


    @Override
    public void enterCategoryDefinition(CategoryDefinitionContext ctx) {
        assert classTemplate == null;
        skipExpressions++;
        classTemplate = new ClassTemplate();
        classTemplate.setClassType(ClassType.CATEGORY);
        fileTemplate.addBodyElement(classTemplate);
        final String className = cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.baseClass);
        classTemplate.setClassName(className);
        int newCategoryNumber = numCategories.getOrDefault(className, 0) + 1;
        numCategories.put(className, newCategoryNumber);
        classTemplate.setSuperClassOrCategoryName("$"+className+"_Cat"+newCategoryNumber);
    }

    @Override
    public void exitCategoryDefinition(CategoryDefinitionContext ctx) {
        classTemplate = null;
        skipExpressions--;
    }


    @Override
    public void enterClassMethodDecs(ClassMethodDecsContext ctx) {
        isStaticMethod = ctx.target.getText().equals("meta_class");
    }

    @Override
    public void exitClassMethodDecs(ClassMethodDecsContext ctx) {
        isStaticMethod = null;
    }

    @Override
    public void enterClassMethodDefinition(ClassMethodDefinitionContext ctx) {
        skipExpressions++;
        methodTemplate = new MethodTemplate();
        classTemplate.addBodyElement(methodTemplate);
        methodTemplate.setReturnType(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.returnType));
        methodTemplate.setAbstract(false);
        methodTemplate.setSelector(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.selector));
        methodTemplate.isStatic(isStaticMethod);
        for (ParamTypeContext paramTypeContext : ctx.paramType()) {
            methodTemplate.addParamType(cappuccino.decompiler.parser.Utils.stripContainingQuotes(paramTypeContext.StringLiteral().getText()));
        }
        for (VarNameContext varNameContext : ctx.varName()) {
            methodTemplate.addVariableName(varNameContext.getText());
        }
    }

    @Override
    public void exitClassMethodDefinition(ClassMethodDefinitionContext ctx) {
        methodTemplate = null;
        skipExpressions--;
    }

    @Override public void enterProtocolDefinition(ProtocolDefinitionContext ctx) {
        assert classTemplate == null;
        classTemplate = new ClassTemplate();
        classTemplate.setClassType(ClassType.PROTOCOL);
        if (fileTemplate != null) {
            fileTemplate.addBodyElement(classTemplate);
        }
        classTemplate.setClassName(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.protocolName));
        skipExpressions++;
    }

    @Override public void exitProtocolDefinition(ProtocolDefinitionContext ctx) {
        classTemplate = null;
        skipExpressions--;
    }

    @Override
    public void enterProtoMethodsDec(ProtoMethodsDecContext ctx) {
        switch(ctx.isInstanceMethods.getText().toUpperCase())
        {
            case "NO":
            case "FALSE":
                isStaticMethod = true;
            default:
                isStaticMethod = false;
        }
    }

    @Override
    public void exitProtoMethodsDec(ProtoMethodsDecContext ctx) {
        isStaticMethod = null;
    }


    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterProtocolMethodDefinition(ProtocolMethodDefinitionContext ctx) {
        //LOGGER.log(Level.INFO, "Entered Protocol Method Definition");
        assert isStaticMethod != null;
        methodTemplate = new MethodTemplate();
        classTemplate.addBodyElement(methodTemplate);

        methodTemplate.isStatic(isStaticMethod);
        methodTemplate.setReturnType(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.returnType));
        methodTemplate.setSelector(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.selector));
        methodTemplate.setAbstract(true);
        if (ctx.paramTypes != null) {
            for (ParamTypeContext paramTypeContext : ctx.paramType())
            methodTemplate.addParamType(cappuccino.decompiler.parser.Utils.stripContainingQuotes(paramTypeContext.StringLiteral().getText()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitProtocolMethodDefinition(ProtocolMethodDefinitionContext ctx) {
        methodTemplate = null;
    }



    @Override
    public void enterVariableStatement(VariableStatementContext ctx) {
        if (skipExpressions > 0) {
            return;
        }
        assert fileTemplate != null : "Cannot add statements to null file template";
        //LOGGER.log(Level.INFO, ctx.getText());
        variableStatementTemplate = new VariableStatementTemplate();
        fileTemplate.addBodyElement(variableStatementTemplate);
    }

    @Override
    public void exitVariableStatement(VariableStatementContext ctx) {
        if (variableStatementTemplate != null) {
            fileTemplate.append("\n");
        }
        variableStatementTemplate = null;
    }


    @Override
    public void enterVariableDeclaration(VariableDeclarationContext ctx) {
        if (skipExpressions > 0 || variableStatementTemplate == null) {
            return;
        }
        variableDeclarationTemplate = new VariableDeclarationTemplate();
        variableStatementTemplate.addAssignments(variableDeclarationTemplate);
        variableDeclarationTemplate.setVarName(ctx.getChild(0).getText());
    }

    @Override
    public void exitVariableDeclaration(VariableDeclarationContext ctx) {
        variableDeclarationTemplate = null;
    }

    @Override
    public void enterFunctionExpression(FunctionExpressionContext ctx) {
        skipExpressions++;
        if (skipExpressions > 1) {
            return;
        }
        FunctionTemplate functionTemplate = new FunctionTemplate();
        if (ctx.functionName != null) {
            functionTemplate.setFunctionName(ctx.functionName.getText());
        }
        appendFunctionParameters(functionTemplate, ctx.formalParameterList());
        if (variableDeclarationTemplate != null) {
            variableDeclarationTemplate.setBody(functionTemplate);
        } else {
            fileTemplate.addBodyElement(functionTemplate);
        }
    }

    @Override
    public void exitFunctionExpression(FunctionExpressionContext ctx) {
        skipExpressions--;
    }

    @Override
    public void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
        skipExpressions++;
        FunctionTemplate functionTemplate = new FunctionTemplate();
        functionTemplate.setFunctionName(ctx.functionName.getText());
        appendFunctionParameters(functionTemplate, ctx.formalParameterList());
        fileTemplate.addBodyElement(functionTemplate);
    }

    @Override
    public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
        skipExpressions--;
    }


    private void appendFunctionParameters(FunctionTemplate functionTemplate, FormalParameterListContext formalParameterListContext) {
        if (formalParameterListContext == null || formalParameterListContext.formalParameterArg().isEmpty()) {
            return;
        }
        for (FormalParameterArgContext argContext : formalParameterListContext.formalParameterArg()) {
            functionTemplate.addParam(argContext.paramName.getText());
        }
        if (formalParameterListContext.lastFormalParameterArg() != null) {
            functionTemplate.addParam("... "+formalParameterListContext.lastFormalParameterArg().paramName.getText());
        }
    }

    @Override
    public void enterIVar(IVarContext ctx) {
        assert classTemplate != null;
        classTemplate.addIVar(cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.name), cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.type.getText()));
    }

    @Override
    public void exitImports(ImportsContext ctx) {
        assert fileTemplate != null;
        fileTemplate.addBodyElement(new StatementTemplate("\n"));
    }

    @Override
    public void enterImportStatement(ImportStatementContext ctx) {
        assert fileTemplate != null;
        String text = "@import ";
        Matcher matchResult = IMPORT_PATTERN.matcher(ctx.getText());
        if (!matchResult.matches()) {
            LOGGER.log(Level.SEVERE, "Import statement does not match expected pattern");
            return;
        }
        final String importText = matchResult.group(1);
        if (ctx.fileImport != null) {
            text += "\""+importText+"\"";
        } else {
            text += "<"+importText+">";
        }
        text += "\n";
        fileTemplate.addBodyElement(new StatementTemplate(text));
    }

    @Override
    public void enterAssignmentExpression(AssignmentExpressionContext ctx) {
        if (skipExpressions > 0) {
            return;
        }
        variableDeclarationTemplate = new VariableDeclarationTemplate();
        variableDeclarationTemplate.setVarName(ctx.singleExpression(0).getText());
        fileTemplate.addBodyElement(variableDeclarationTemplate);
        fileTemplate.append(";\n");
    }

    @Override
    public void exitAssignmentExpression(AssignmentExpressionContext ctx) {
        variableDeclarationTemplate = null;
    }

    @Override
    public void enterTypedefDefinition(TypedefDefinitionContext ctx) {
        fileTemplate.append("@typedef "+ cappuccino.decompiler.parser.Utils.stripContainingQuotes(ctx.type)+"\n");
    }

    @Override
    public void enterInheritsProtoStatement(InheritsProtoStatementContext ctx) {
        assert classTemplate != null;
        classTemplate.addProtocol(Utils.stripContainingQuotes(ctx.protocolName));
    }

}
