package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.utils.*

import java.util.ArrayList
import java.util.Collections
import java.util.logging.Logger
import java.util.regex.Pattern

import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getIndexInQualifiedNameParent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getPrecedingVariableAssignmentNameElements
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getQualifiedNameAsString

object ObjJVariableNameCompletionContributorUtil {

    private val LOGGER = Logger.getLogger(ObjJVariableNameCompletionContributorUtil::class.java.name)
    val CARET_INDICATOR = ObjJCompletionContributor.CARET_INDICATOR

    fun getVariableNameCompletions(variableName: ObjJVariableName?): List<String> {
        if (variableName == null || variableName.text.isEmpty()) {
            return emptyList()
        }
        val qualifiedNameIndex = getIndexInQualifiedNameParent(variableName)
        val out = ArrayList<String>()
        val variableNamePattern = getQualifiedNameAsString(variableName).replace(CARET_INDICATOR, "(.*)")
        val pattern = Pattern.compile(variableNamePattern)
        val rawCompletionElements = getPrecedingVariableAssignmentNameElements(variableName, qualifiedNameIndex)
        for (currentVariableName in ArrayUtils.filter(rawCompletionElements) { `var` ->
            //LOGGER.log(Level.INFO, "Checking var: <"+var.getText()+"> for match");
            pattern.matcher(getQualifiedNameAsString(`var`)).matches()
        }) {
            //LOGGER.log(Level.INFO, "Adding Value to completion results.");
            out.add(currentVariableName.name)
        }
        //LOGGER.log(Level.INFO, String.format("NameFilter:<%s>; Raw Completion Elements: <%d>; Num after filter by name: <%d>", variableName, rawCompletionElements.size(), out.size()));
        return out
    }

    /*
    @NotNull
    public static List<String> getVariableNameCompletions(@Nullable ObjJVariableName variableName) {
        if (variableName == null || variableName.getText().isEmpty()) {
            return Collections.emptyList();
        }
        final int qualifiedNameIndex = getIndexInQualifiedNameParent(variableName);
        final List<String> out = new ArrayList<>();
        final String variableNamePattern = getQualifiedNameAsString(variableName).replace(CARET_INDICATOR, "(.*)");
        final Pattern pattern = Pattern.compile(variableNamePattern);
        List<ObjJVariableName> rawCompletionElements = getPrecedingVariableNameElements(variableName, qualifiedNameIndex);
        for (ObjJVariableName currentVariableName : ArrayUtils.filter(rawCompletionElements, (var) -> {
            LOGGER.log(Level.INFO, "Checking var: <"+var.getText()+"> for match");
            return pattern.matcher(getQualifiedNameAsString(var)).matches();
        })) {
            LOGGER.log(Level.INFO, "Adding Value to completion results.");
            out.add(currentVariableName.getName());
        }
        LOGGER.log(Level.INFO, String.format("NameFilter:<%s>; Raw Completion Elements: <%d>; Num after filter by name: <%d>", variableName, rawCompletionElements.size(), out.size()));
        return out;
    }

    @NotNull
    public static List<ObjJVariableName> getMatchingPrecedingVariableNameElements(final PsiElement variableName, int qualifiedIndex) {
        final int startOffset = variableName.getTextRange().getStartOffset();
        String variableNameQualifiedString;
        if (variableName instanceof ObjJVariableName) {
            variableNameQualifiedString = getQualifiedNameAsString((ObjJVariableName)variableName);
        } else {
            variableNameQualifiedString = variableName.getText();
        }
        return getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex, (var) -> {
            String varsFqName = getQualifiedNameAsString(var);
            LOGGER.log(Level.INFO, "getMatchingPrecedingVariableNameElements: <"+variableNameQualifiedString+"> ?= <"+varsFqName+">");
            return var != variableName && variableNameQualifiedString.equals(varsFqName) && var.getTextRange().getStartOffset() < startOffset;
        });
    }

    @NotNull
    public static String getQualifiedNameAsString(@NotNull ObjJVariableName variableName) {
        ObjJQualifiedReference qualifiedReference = variableName.getParentOfType( ObjJQualifiedReference.class);
        if (qualifiedReference == null) {
            return variableName.getText();
        }
        List<ObjJVariableName> variableNames = qualifiedReference.getVariableNameList();
        if (variableNames.isEmpty()) {
            return variableName.getText();
        }
        int index = getIndexInQualifiedNameParent(variableName);
        StringBuilder builder = new StringBuilder(variableNames.get(0).getText());
        for (int i=1;i<=index && i<variableNames.size();i++) {
            builder.append(".").append(variableNames.get(i).getText());
        }
        //LOGGER.log(Level.INFO, "Qualified name is: <"+builder.toString()+"> for var in file: "+variableName.getContainingFile().getVirtualFile().getName()+"> at offset: <"+variableName.getTextRange().getStartOffset()+">");
        return builder.toString();
    }

    @NotNull
    public static List<ObjJVariableName> getPrecedingVariableNameElements(final PsiElement variableName, int qualifiedIndex) {
        final int startOffset = variableName.getTextRange().getStartOffset();
        PsiFile file = variableName.getContainingFile();
        LOGGER.log(Level.INFO, String.format("Qualified Index: <%d>; TextOffset: <%d>; TextRange: <%d,%d>", qualifiedIndex, variableName.getTextOffset(), variableName.getTextRange().getStartOffset(), variableName.getTextRange().getEndOffset()));
        return getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex, (var) -> var != variableName && (var.getContainingFile().isEquivalentTo(file) || var.getTextRange().getStartOffset() < startOffset));
    }

    @NotNull
    public static List<ObjJVariableName> getAndFilterSiblingVariableNameElements(PsiElement element, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        List<ObjJVariableName> rawVariableNameElements = getSiblingVariableNameElements(element, qualifiedNameIndex);
        List<ObjJVariableName> out = ArrayUtils.filter(rawVariableNameElements, filter);
        LOGGER.log(Level.INFO, String.format("Get Siblings by var name before filter. BeforeFilter<%d>; AfterFilter:<%d>", rawVariableNameElements.size(), out.size()));
        return out;
    }

    @NotNull
    public static List<ObjJVariableName> getSiblingVariableNameElements(PsiElement element, int qualifiedNameIndex) {
        ArrayList<ObjJVariableName> result = new ArrayList<>();
        ObjJBlock block = element instanceof ObjJBlock ? ((ObjJBlock)element) : PsiTreeUtil.getParentOfType(element, ObjJBlock.class);
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true);
        bodyVariableAssignments.addAll(ObjJBlockPsiUtil.getParentBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true));
        for (ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled();
            addAllVariablesFromBodyVariableAssignment(result, bodyVariableAssignment, qualifiedNameIndex);
        }
        int currentSize = result.size();
        LOGGER.log(Level.INFO, "Num from blocks: <"+currentSize+">");
        if (element instanceof ObjJHasContainingClass) {
            if (!DumbService.getInstance(element.getProject()).isDumb()) {
                //addAllClassVariables(result, ((ObjJHasContainingClass) element).getContainingClassName(), element.getProject());
                LOGGER.log(Level.INFO, "Num VariableNames after getting class vars: <" + (result.size() - currentSize) + ">");
                currentSize = result.size();
            }
        }
        if (qualifiedNameIndex <= 1) {
            addAllInstanceVariables(result, element);
            LOGGER.log(Level.INFO, "Num VariableNames after class vars: <"+(result.size()-currentSize)+">");
            currentSize = result.size();
        }
        addAllMethodDeclarationSelectorVars(result, element);
        LOGGER.log(Level.INFO, "Num VariableNames after header declaration vars: <"+(result.size()-currentSize)+">");

        addAllIterationVariables(result, element.getParentOfType( ObjJIterationStatement.class));
        addAllFileScopedVariables(result, element.getContainingFile(), qualifiedNameIndex);
        addAllFunctionScopedVariables(result, element.getParentOfType( ObjJFunctionDeclarationElement.class));
        addCatchProductionVariables(result, element.getParentOfType( ObjJCatchProduction.class));
        addPreprocessorDefineFunctionVariables(result, element.getParentOfType( ObjJPreprocessorDefineFunction.class));
        LOGGER.log(Level.INFO, "Num VariableNames after getting file vars: <"+(result.size()-currentSize)+">");
        return result;

    }

    private static void addAllInstanceVariables(List<ObjJVariableName> result, PsiElement element) {
        if (DumbService.getInstance(element.getProject()).isDumb()) {
            return;
        }
        final String containingClassName = element instanceof ObjJHasContainingClass ? ((ObjJHasContainingClass)element).getContainingClassName() : null;
        if (containingClassName == null || ObjJMethodCallPsiUtil.isUniversalMethodCaller(containingClassName)) {
            return;
        }
        for(String variableHoldingClassName : ObjJClassDeclarationPsiUtil.getAllInheritedClasses(containingClassName, element.getProject())) {
            ProgressIndicatorProvider.checkCanceled();
            for (ObjJInstanceVariableDeclaration declaration : ObjJInstanceVariablesByClassIndex.getInstance().get(variableHoldingClassName, element.getProject())) {
                ProgressIndicatorProvider.checkCanceled();
                result.add(declaration.getVariableName());
            }
        }
    }

    private static void addAllMethodDeclarationSelectorVars(List<ObjJVariableName> result, PsiElement element) {
        ObjJMethodDeclaration declaration = element.getParentOfType(ObjJMethodDeclaration.class);
        if (declaration != null) {
            for (ObjJMethodDeclarationSelector methodDeclarationSelector : declaration.getMethodHeader().getMethodDeclarationSelectorList()) {
                ProgressIndicatorProvider.checkCanceled();
                if (methodDeclarationSelector.getVariableName() == null || methodDeclarationSelector.getVariableName() == null || methodDeclarationSelector.getVariableName().getText().isEmpty()) {
                    continue;
                }
                result.add(methodDeclarationSelector.getVariableName());
            }
        }
    }

    private static int getIndexInQualifiedNameParent(@Nullable ObjJVariableName variableName) {
        if (variableName == null) {
            return 0;
        }
        ObjJQualifiedReference qualifiedReferenceParent = variableName.getParentOfType( ObjJQualifiedReference.class);
        int qualifiedNameIndex = qualifiedReferenceParent != null ? qualifiedReferenceParent.getVariableNameList().indexOf(variableName) : -1;
        if (qualifiedNameIndex < 0) {
            qualifiedNameIndex = 0;
        }
        return qualifiedNameIndex;
    }

    private static void addAllFileScopedVariables(@NotNull List<ObjJVariableName> result, @Nullable PsiFile file, int qualifiedNameIndex) {
        if (file == null) {
            LOGGER.log(Level.INFO, "Cannot get all file scoped variables. File is null");
            return;
        }
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = file.getChildrenOfType( ObjJBodyVariableAssignment.class);
        addAllVariablesFromBodyVariableAssignmentsList(result, bodyVariableAssignments, qualifiedNameIndex);
        addAllFileScopeGlobalVariables(result, file);
    }

    private static void addAllFileScopeGlobalVariables(List<ObjJVariableName> result, PsiFile file) {
        List<ObjJExpr> expressions = file.getChildrenOfType( ObjJExpr.class);
        for (ObjJExpr expr : expressions) {
            ProgressIndicatorProvider.checkCanceled();
            if (expr == null || expr.getLeftExpr() == null || expr.getLeftExpr().getVariableDeclaration() == null) {
                continue;
            }
            ObjJVariableDeclaration declaration = expr.getLeftExpr().getVariableDeclaration();
            for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                if (qualifiedReference.getPrimaryVar() != null) {
                    result.add(qualifiedReference.getPrimaryVar());
                }
            }
        }
    }

    private static void addAllVariablesFromBodyVariableAssignmentsList(@NotNull List<ObjJVariableName> result, @NotNull List<ObjJBodyVariableAssignment> bodyVariableAssignments, int qualifiedNameIndex) {
        for(ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled();
            LOGGER.log(Level.INFO, "Body variable assignment: <"+bodyVariableAssignment.getText()+">");
            addAllVariablesFromBodyVariableAssignment(result, bodyVariableAssignment, qualifiedNameIndex);
        }
    }

    private static void addAllVariablesFromBodyVariableAssignment(@NotNull List<ObjJVariableName> result, @Nullable ObjJBodyVariableAssignment bodyVariableAssignment, int qualifiedNameIndex) {
        if (bodyVariableAssignment == null) {
            return;
        }
        List<ObjJQualifiedReference> references = bodyVariableAssignment.getQualifiedReferenceList();
        for (ObjJVariableDeclaration variableDeclaration : bodyVariableAssignment.getVariableDeclarationList()) {
            LOGGER.log(Level.INFO,"VariableDec: <"+variableDeclaration.getText()+">");
            references.addAll(variableDeclaration.getQualifiedReferenceList());
        }
        for (ObjJQualifiedReference qualifiedReference : references) {
            LOGGER.log(Level.INFO, "Checking variable dec for qualified reference: <"+qualifiedReference.getText()+">");
            if (qualifiedNameIndex == -1) {
                result.addAll(qualifiedReference.getVariableNameList());
            } else if (qualifiedReference.getVariableNameList().size() > qualifiedNameIndex) {
                ObjJVariableName suggestion = qualifiedReference.getVariableNameList().get(qualifiedNameIndex);
                result.add(suggestion);
            } else {
                LOGGER.log(Level.INFO, "Not adding variable <"+qualifiedReference.getText()+"> as Index is out of bounds.");
            }
        }
    }

    public static void addAllFunctionScopedVariables(@NotNull List<ObjJVariableName> result, @Nullable ObjJFunctionDeclarationElement functionDeclarationElement) {
        if (functionDeclarationElement == null) {
            return;
        }
        for (Object parameterArg : functionDeclarationElement.getFormalParameterArgList()) {
            result.add(((ObjJFormalParameterArg)parameterArg).getVariableName());
        }
    }

    public static void addAllIterationVariables(@NotNull List<ObjJVariableName> result, @Nullable ObjJIterationStatement iterationStatement) {
        if (iterationStatement == null) {
            return;
        }
        for (ObjJVariableDeclaration declaration : iterationStatement.getVariableDeclarationList()) {
            ProgressIndicatorProvider.checkCanceled();
            LOGGER.log(Level.INFO, "Adding all iteration statement variables for dec: <"+declaration.getText()+">");
            for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                result.add(qualifiedReference.getPrimaryVar());
            }
        }
    }

    public static void addCatchProductionVariables(@NotNull List<ObjJVariableName> result, @Nullable ObjJCatchProduction catchProduction) {
        if (catchProduction == null) {
            return;
        }
        result.add(catchProduction.getVariableName());
    }

    public static void addPreprocessorDefineFunctionVariables(@NotNull List<ObjJVariableName> result, @Nullable ObjJPreprocessorDefineFunction function) {
        if (function == null) {
            return;
        }
        for (ObjJFormalParameterArg formalParameterArg : function.getFormalParameterArgList()) {
            result.add(formalParameterArg.getVariableName());
        }
    }
    */
}
