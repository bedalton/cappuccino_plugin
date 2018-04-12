package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StubElement;
import com.intellij.usageView.UsageViewUtil;
import javafx.scene.control.ProgressIndicator;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJCompositeElementImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJClassDeclarationPsiUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJClassDeclarationPsiUtil.class.getCanonicalName());

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
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(containingClass.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        List<ObjJClassDeclarationElement> superClassDeclarations = new ArrayList<>(ObjJClassDeclarationsIndex.getInstance().get(superClassName, childElement.getProject()));
        if (superClassDeclarations.size() < 1) {
         //   LOGGER.log(Level.INFO, "Super class references an undefined class <"+superClassName+">");
            return returnDefault ? containingClass.getClassName() : null;
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
    public static String getSuperClassName(@NotNull String className, @NotNull Project project) {
        if (DumbService.isDumb(project)) {
            throw new IndexNotReadyRuntimeException();
        }
        for (ObjJImplementationDeclaration declaration : ObjJImplementationDeclarationsIndex.getInstance().get(className, project)) {
            if (declaration.getSuperClassName() != null) {
                return declaration.getSuperClassName();
            }
        }
        return null;
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


    public static Map<ObjJClassName, ProtocolMethods> getAllUnimplementedProtocolMethods(@NotNull ObjJImplementationDeclaration declaration) {
        //todo
        return Collections.emptyMap();
    }


    public static ProtocolMethods getUnimplementedProtocolMethods(@NotNull ObjJImplementationDeclaration declaration, @NotNull String protocolName) {
        ObjJInheritedProtocolList protocolListElement = declaration.getInheritedProtocolList();
        if (protocolListElement == null) {
            return ObjJProtocolDeclarationPsiUtil.EMPTY_PROTOCOL_METHODS_RESULT;
        }
        if (DumbService.isDumb(declaration.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }

        final String thisClassName = declaration.getClassNameString();
        final Project project = declaration.getProject();
        List<ObjJMethodHeader> required = new ArrayList<>();
        List<ObjJMethodHeader> optional = new ArrayList<>();
        List<ObjJProtocolDeclaration> inheritedProtocols = ObjJInheritanceUtil.getAllInheritedProtocols(protocolName, declaration.getProject());
        for (ObjJProtocolDeclaration protocolDeclaration : inheritedProtocols) {
            Logger.getAnonymousLogger().log(Level.INFO, "Checking protocol <"+protocolDeclaration.getClassNameString()+"> for unimplemented methods.");
            addUnimplementedProtocolMethods(project, thisClassName, protocolDeclaration.getMethodHeaderList(), required);
            for (ObjJProtocolScopedBlock scopedBlock : protocolDeclaration.getProtocolScopedBlockList()) {
                ProgressIndicatorProvider.checkCanceled();
                List<ObjJMethodHeader> headerList = scopedBlock.getAtOptional() != null ? optional : required;
                addUnimplementedProtocolMethods(project, thisClassName, scopedBlock.getMethodHeaderList(), headerList);
            }
        }
        return new ProtocolMethods(required, optional);
    }

    private static void addUnimplementedProtocolMethods(@NotNull Project project, @NotNull String className,
                                                        @NotNull List<ObjJMethodHeader> requiredHeaders, @NotNull List<ObjJMethodHeader> listOfUnimplemented) {
        for (ObjJMethodHeader methodHeader : requiredHeaders) {
            ProgressIndicatorProvider.checkCanceled();
            if (!isProtocolMethodImplemented(project, className, methodHeader.getSelectorString())) {
                listOfUnimplemented.add(methodHeader);
            }
        }
    }

    private static boolean isProtocolMethodImplemented(@NotNull Project project, @NotNull String className, @NotNull String selector) {
        while (className != null) {
            ProgressIndicatorProvider.checkCanceled();
            for (ObjJMethodHeader methodHeaderDeclaration : ObjJClassMethodIndex.getInstance().get(className, project)) {
                if (methodHeaderDeclaration.getSelectorString().equals(selector)) {
                    LOGGER.log(Level.INFO, "Class <"+className+"> has required protocol method: <"+selector+"> in class: "+methodHeaderDeclaration.getContainingClassName()+ " in file "+ObjJFileUtil.getContainingFileName(methodHeaderDeclaration));
                    return true;
                }
            }
            LOGGER.log(Level.INFO, "Class <"+className+"> does not have required protocol method: <"+selector+">");
            className = ObjJClassDeclarationPsiUtil.getSuperClassName(className, project);
        }
        return false;
    }


    public static ItemPresentation getPresentation(@NotNull final ObjJImplementationDeclaration implementationDeclaration) {
        LOGGER.log(Level.INFO, "Get Presentation <Implementation:"+implementationDeclaration.getClassNameString()+">");
        final String text = implementationDeclaration.getClassNameString() + (implementationDeclaration.isCategory() ? " ("+implementationDeclaration.getCategoryName()+")" : "");
        final Icon icon = implementationDeclaration.isCategory() ? ObjJIcons.CATEGORY_ICON : ObjJIcons.CLASS_ICON;
        final String fileName = ObjJFileUtil.getContainingFileName(implementationDeclaration);
        return new ItemPresentation() {
            @NotNull
            @Override
            public String getPresentableText() {
                return text;
            }

            @NotNull
            @Override
            public String getLocationString() {
                return fileName != null ? fileName : "";
            }

            @NotNull
            @Override
            public Icon getIcon(boolean b) {
                return icon;
            }
        };
    }

    public static ItemPresentation getPresentation(@NotNull final ObjJProtocolDeclaration protocolDeclaration) {
        LOGGER.log(Level.INFO, "Get Presentation <Protocol:"+protocolDeclaration.getClassNameString()+">");
        final String fileName = ObjJFileUtil.getContainingFileName(protocolDeclaration);
        return new ItemPresentation() {
            @NotNull
            @Override
            public String getPresentableText() {
                return protocolDeclaration.getClassNameString();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return fileName != null ? fileName : "";
            }

            @NotNull
            @Override
            public Icon getIcon(boolean b) {
                return ObjJIcons.PROTOCOL_ICON;
            }
        };
    }

}
