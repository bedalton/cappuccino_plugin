package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.project.DumbService;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeReference;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeResults;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.UNDEF_CLASS_NAME;
import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED;
import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.isPrimitive;

public class ObjJMethodCallPsiUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJMethodCallPsiUtil.class.getName());
    private static final String GET_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("class");
    private static final String GET_SUPERCLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("superclass");
    public static final String IS_KIND_OF_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("isKindOfClass");

    public static String getSelectorString(ObjJMethodCall methodCall) {
        ObjJMethodCallStub stub = methodCall.getStub();
        if (stub != null) {
            //LOGGER.log(Level.INFO, "Getting method call selector string from stub: <"+stub.getSelectorString()+">");
            return stub.getSelectorString();
        }
        return ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(methodCall.getSelectorStrings());
    }


    public static boolean isUniversalMethodCaller(@NotNull final String className) {
        return !isPrimitive(className) &&  (UNDETERMINED.equals(className) || UNDEF_CLASS_NAME.equals(className));
    }

    @NotNull
    public static List<String> getSelectorStrings(@NotNull ObjJMethodCall methodCall) {
        if (methodCall.getStub() != null && !methodCall.getStub().getSelectorStrings().isEmpty()) {
            return methodCall.getStub().getSelectorStrings();
        }
        return ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(getSelectorList(methodCall));
    }

    @NotNull
    public static List<ObjJSelector> getSelectorList(@NotNull ObjJMethodCall methodCall) {
        if (methodCall.getSelector() != null) {
            return Collections.singletonList(methodCall.getSelector());
        }
        final List<ObjJSelector> out = new ArrayList<>();
        for (ObjJQualifiedMethodCallSelector qualifiedSelector : methodCall.getQualifiedMethodCallSelectorList()) {
            out.add(qualifiedSelector.getSelector());
        }
        return out;
    }

    @NotNull
    public static String getCallTargetText(ObjJMethodCall methodCall) {
        if (methodCall.getStub() != null) {
            return methodCall.getStub().getCallTarget();
        }
        return  methodCall.getCallTarget().getText();
    }

}
