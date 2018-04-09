package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.exceptions.CannotDetermineException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED;

public class ObjJClassDeclarationPsiUtil {

    public static boolean isCategory(@NotNull ObjJImplementationDeclaration implementationDeclaration) {
        return implementationDeclaration.getCategoryName() != null;
    }

    @NotNull
    public static List<ObjJClassName> getAllClassNameElements(Project project) {
        final ObjJClassDeclarationsIndex classDeclarationsIndex = ObjJClassDeclarationsIndex.getInstance();
        Collection<String> classNameStrings = classDeclarationsIndex.getAllKeys(project);
        List<ObjJClassName> classNameElements = new ArrayList<>();
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(project)) {
            throw new IndexNotReadyRuntimeException();
        }
        for (String classNameString : classNameStrings) {
            Collection<ObjJClassDeclarationElement> decs = classDeclarationsIndex.get(classNameString, project);
            for (ObjJClassDeclarationElement dec  :decs ){
                final ObjJClassName classNameElement = dec.getClassName();
                if (classNameElements.contains(classNameElement)) {
                    continue;
                }
                classNameElements.add(classNameElement);
            }
        }
        return classNameElements;
    }


    /**
     * Gets the super class of the containing class declaration
     * @param childElement child element of a class declaration with a super class
     * @param returnDefault <b>true</b> if method should return containing class if super class is not found
     * @return containing super class
     */
    @Nullable
    public static ObjJClassName getContainingSuperClass(ObjJCompositeElement childElement, boolean returnDefault) {
        ObjJClassDeclarationElement containingClass = ObjJPsiImplUtil.getContainingClass(childElement);
        if (containingClass == null) {
         //   LOGGER.log(Level.INFO, "Child element of type <"+childElement.getNode().getElementType().toString()+"> has no containing superclass.");
            return null;
        }

        if (!(containingClass instanceof ObjJImplementationDeclaration)) {
            return returnDefault ? containingClass.getClassName() : null;
        }

        ObjJImplementationDeclaration implementationDeclaration = ((ObjJImplementationDeclaration) containingClass);
        String superClassName = !implementationDeclaration.isCategory() ? implementationDeclaration.getSuperClassName() : implementationDeclaration.getClassNameString();
        if (superClassName == null) {
         //   LOGGER.log(Level.INFO, "Cannot get super declaration as class has no super class.");
            return returnDefault ? containingClass.getClassName() : null;
        }
        List<ObjJClassDeclarationElement> superClassDeclarations = new ArrayList<>(ObjJClassDeclarationsIndex.getInstance().get(superClassName, childElement.getProject()));
        if (superClassDeclarations.size() < 1) {
         //   LOGGER.log(Level.INFO, "Super class references an undefined class <"+superClassName+">");
            return returnDefault ? containingClass.getClassName() : null;
        }
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(containingClass.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        for (ObjJClassDeclarationElement superClassDec : superClassDeclarations) {
            if (superClassDec instanceof ObjJImplementationDeclaration) {
                ObjJImplementationDeclaration superClassImplementationDeclaration = ((ObjJImplementationDeclaration) superClassDec);
                if (superClassImplementationDeclaration.isEquivalentTo(containingClass) || superClassImplementationDeclaration.isCategory()) {
                    continue;
                }
                return superClassDeclarations.get(0).getClassName();
            }
        }
        return null;
    }

    public static void addProtocols(
            @NotNull
                    ObjJClassDeclarationElement classDeclarationElement,
            @NotNull
                    List<String> protocols) {
        StubElement stub = classDeclarationElement.getStub();
        List newProtocols;
        if (stub != null) {
            ObjJClassDeclarationStub stubElement = (ObjJClassDeclarationStub)stub;
            newProtocols = stubElement.getInheritedProtocols();
        } else {
            newProtocols = classDeclarationElement.getInheritedProtocols();
        }
        for (Object proto : newProtocols) {
            String protocol = (String) proto;
            if (protocols.contains(protocol)) {
                continue;
            }
            protocols.add(protocol);
        }
    }

    @Nullable
    public static String getSuperClassName(ObjJImplementationDeclaration implementationDeclaration) {
        ObjJImplementationStub stub = implementationDeclaration.getStub();
        return stub != null ? stub.getSuperClassName() : implementationDeclaration.getSuperClass() != null ? implementationDeclaration.getSuperClass().getText() : null;
    }

    @NotNull
    public static List<ObjJMethodHeader> getMethodHeaders(ObjJImplementationDeclaration declaration) {
        List<ObjJMethodHeader> headers = new ArrayList<>();
        for (ObjJMethodDeclaration methodHeaderDeclaration : declaration.getMethodDeclarationList()) {
            headers.add(methodHeaderDeclaration.getMethodHeader());
        }
        return headers;
    }

    @NotNull
    public static List<ObjJMethodHeader> getMethodHeaders(ObjJProtocolDeclaration protocolDeclaration) {
        List<ObjJMethodHeader> headers = protocolDeclaration.getMethodHeaderList();
        for (ObjJProtocolScopedBlock scopedBlock : protocolDeclaration.getProtocolScopedBlockList()) {
            headers.addAll(scopedBlock.getMethodHeaderList());
        }
        return headers;
    }

    public static boolean hasMethod(@NotNull ObjJProtocolDeclaration classDeclarationElement, @NotNull String selector) {
        for (ObjJMethodHeaderDeclaration methodHeader : classDeclarationElement.getMethodHeaders()) {
            if (methodHeader.getSelectorString().equals(selector)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasMethod(@NotNull ObjJImplementationDeclaration classDeclaration, @NotNull String selector) {
        for (ObjJMethodHeaderDeclaration methodHeader : classDeclaration.getMethodHeaders()) {
            if (methodHeader.getSelectorString().equals(selector)) {
                return true;
            }
        }
        if (classDeclaration.getInstanceVariableList() != null) {
            for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : classDeclaration.getInstanceVariableList().getInstanceVariableDeclarationList()) {
                if (instanceVariableDeclaration.getAtAccessors() == null) {
                    continue;
                }
                if (instanceVariableDeclaration.getVariableName() != null && selector.equals(instanceVariableDeclaration.getVariableName().getText())) {
                    return true;
                }
                if (instanceVariableDeclaration.getAccessorPropertyList().isEmpty()) {
                    if (selector.startsWith("set") && selector.substring(2).equals(instanceVariableDeclaration.getVariableName().getText())) {
                        return true;
                    }
                }
                for (ObjJAccessorProperty accessorProperty : instanceVariableDeclaration.getAccessorPropertyList()) {
                    if (selector.equals(accessorProperty.getSelectorString()) || selector.startsWith("set") && selector.substring(2).equals(accessorProperty.getSelectorString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
