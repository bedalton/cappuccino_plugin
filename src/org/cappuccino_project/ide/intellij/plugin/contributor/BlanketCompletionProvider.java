package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler;
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.contributor.ObjJCompletionContributor.CARET_INDICATOR;
import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.PRIMITIVE_VAR_NAMES;
import static org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY;

public class BlanketCompletionProvider  extends CompletionProvider<CompletionParameters> {

    private static final Logger LOGGER = Logger.getLogger(BlanketCompletionProvider.class.getName());

    private static final List<String> ACCESSSOR_PROPERTY_TYPES = Arrays.asList("property", "getter", "setter", "readonly", "copy");


    @Override
    protected void addCompletions(
            @NotNull
                    CompletionParameters parameters, ProcessingContext context,
            @NotNull
                    CompletionResultSet resultSet) {
        //LOGGER.log(Level.INFO, "Trying to get completion parameters.");
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        /*LOGGER.log(Level.INFO,
                "Parent is of type: <"+parent.getNode().getElementType().toString()+"> with value: <"+parent.getText()+">\n"
                        +   "Child is of type <"+element.getNode().getElementType()+"> with text <"+element.getText()+">"
        );*/
        List<String> results;
        final String queryString = element.getText().substring(0, element.getText().indexOf(CARET_INDICATOR));
        if (element instanceof ObjJAccessorPropertyType || ObjJTreeUtil.getParentOfType(element, ObjJAccessorPropertyType.class) != null) {
            results = ArrayUtils.search(ACCESSSOR_PROPERTY_TYPES, queryString);
        } else if (element instanceof ObjJVariableName || parent instanceof ObjJVariableName) {
            if (queryString.trim().isEmpty() ) {
                //LOGGER.log(Level.INFO, "Query string is empty");
                resultSet.stopHere();
                return;
            }
            if (ObjJVariablePsiUtil.isNewVarDec(element)) {
                resultSet.stopHere();
                return;
            }
            ObjJVariableName variableName =(ObjJVariableName)(element instanceof ObjJVariableName ? element : parent);
            results = ObjJVariableNameCompletionContributorUtil.getVariableNameCompletions(variableName);
            appendFunctionCompletions(resultSet, element);
            results.addAll(getKeywordCompletions(variableName));
            results.addAll(getInClassKeywords(variableName));
        } else if (PsiTreeUtil.getParentOfType(element, ObjJMethodCall.class) != null) {
            //LOGGER.log(Level.INFO, "Searching for selector completions.");
            ObjJMethodCallCompletionContributorUtil.addSelectorLookupElementsFromSelectorList(resultSet, element);
            return;
        } else if (PsiTreeUtil.getParentOfType(element, ObjJInheritedProtocolList.class) != null) {
            results = ObjJProtocolDeclarationsIndex.getInstance().getKeysByPattern(queryString+"(.+)", element.getProject());
        } else {
            results = new ArrayList<>();
        }
        results.addAll(getClassNameCompletions(element));

        if (results.isEmpty()) {
            resultSet.stopHere();
        }
        addCompletionElementsSimple(resultSet, results);
    }

    private List<String> getClassNameCompletions(@Nullable
                                                         PsiElement element) {
        if (element == null) {
            return EMPTY_STRING_ARRAY;
        }
        boolean doSearch = false;
        ObjJArrayLiteral arrayLiteral = ObjJTreeUtil.getParentOfType(element, ObjJArrayLiteral.class);
        if (arrayLiteral != null && arrayLiteral.getAtOpenbracket() == null && arrayLiteral.getExprList().size() == 1) {
            doSearch = true;
        }
        ASTNode prev = ObjJTreeUtil.getPreviousNonEmptyNode(element, true);
        if (prev != null && prev.getText().equals("<")) {
            prev = ObjJTreeUtil.getPreviousNonEmptyNode(prev.getPsi(), true);
            if (prev != null && prev.getText().equals("id")) {
                doSearch = true;
            }
        }
        ObjJCallTarget callTarget = ObjJTreeUtil.getParentOfType(element, ObjJCallTarget.class);
        if (callTarget != null) {
            doSearch = true;
        }
        if (!doSearch) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> results = ObjJClassDeclarationsIndex.getInstance().getKeysByPattern(element.getText().replace(CARET_INDICATOR, "(.*)"), element.getProject());
        results.addAll(ArrayUtils.search(PRIMITIVE_VAR_NAMES, element.getText().substring(0, element.getText().indexOf(CARET_INDICATOR))));
        results.addAll(ArrayUtils.search(Arrays.asList("self", "super"), element.getText().substring(0, element.getText().indexOf(CARET_INDICATOR))));
        return results;
    }

    private List<String> getInClassKeywords(@Nullable PsiElement element) {
        if (element == null) {
            return EMPTY_STRING_ARRAY;
        }
        final String queryText = element.getText().substring(0, element.getText().indexOf(CARET_INDICATOR));
        List<String> out = new ArrayList<>();
        if (ObjJTreeUtil.getParentOfType(element, ObjJClassDeclarationElement.class) != null) {
            if ("self".startsWith(queryText)) {
                out.add("self");
            }
            if ("super".startsWith(queryText)) {
                out.add("super");
            }
        }
        return out;
    }

    private List<String> getKeywordCompletions(@Nullable PsiElement element) {
        ObjJExpr expression = ObjJTreeUtil.getParentOfType(element, ObjJExpr.class);
        if (expression == null || !expression.getText().equals(element.getText()) || !(expression.getParent() instanceof ObjJBlock)) {
            return  EMPTY_STRING_ARRAY;
        }
        return ObjJKeywordsList.search(element.getText().substring(0, element.getText().indexOf(CARET_INDICATOR)));
    }

    private void addCompletionElementsSimple(CompletionResultSet resultSet, List<String> completionOptions) {
        for (String completionOption : completionOptions) {
            ProgressIndicatorProvider.checkCanceled();
            resultSet.addElement(LookupElementBuilder.create(completionOption).withInsertHandler(ObjJVariableInsertHandler.getInstance()));
        }
    }

    private void appendFunctionCompletions(CompletionResultSet resultSet, PsiElement element) {
        final String functionNamePattern = element.getText().replace(CARET_INDICATOR, "(.*)");
        Map<String, List<ObjJFunctionDeclarationElement>> functions = ObjJFunctionsIndex.getInstance().getByPattern(functionNamePattern, element.getProject());
        for (String functionName : functions.keySet()) {
            for (ObjJFunctionDeclarationElement function : functions.get(functionName) ) {
                ProgressIndicatorProvider.checkCanceled();
                double priority = PsiTreeUtil.findCommonContext(function, element) != null ? ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY : ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY;
                //noinspection unchecked
                LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                                .create(functionName)
                                .withTailText("(" + ArrayUtils.join(((List<String>) function.getParamNames()), ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                                .withInsertHandler(ObjJFunctionNameInsertHandler.getInstance());
                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority));
            }
        }
    }
}