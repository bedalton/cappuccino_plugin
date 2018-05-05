package org.cappuccino_project.ide.intellij.plugin.utils;

import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.cappuccino_project.ide.intellij.plugin.exceptions.CannotDetermineException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassInheritanceIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInheritedProtocolList;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJClassDeclarationPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED;

public class ObjJInheritanceUtil {

    /**
     * Meant to take an array of inheritance, and reduce it to the deepest descendant in the tree
     * @param classList list of classes
     * @param project project
     * @return list of deepest descendants
     * @throws IndexNotReadyInterruptingException thrown if index is not ready
     */
    public static List<String> reduceToDeepestInheritance(@NotNull List<String> classList, @NotNull Project project) throws IndexNotReadyInterruptingException {
        List<String> superClasses = new ArrayList<>();
        List<String> out = new ArrayList<>(classList);
        if (DumbService.isDumb(project)) {
            throw new IndexNotReadyInterruptingException();
        }
        for (String className : classList) {
            if (className == null) {
                continue;
            }
            if (superClasses.contains(className)) {
                out.remove(className);
                continue;
            }
            for (String parentClassName : getAllInheritedClasses(className, project)) {
                if (out.contains(parentClassName)) {
                    out.remove(parentClassName);
                }
                superClasses.add(parentClassName);
            }
            out.add(className);
        }
        return superClasses;
    }

    @NotNull
    public static List<String> getAllInheritedClasses(@NotNull String className, @NotNull Project project) {
        List<String> inheritedClasses = new ArrayList<>();
        getAllInheritedClasses(inheritedClasses, className, project);
        return inheritedClasses;
    }


    @NotNull
    public static List<ObjJProtocolDeclaration> getAllInheritedProtocols(@NotNull String className, @NotNull Project project) {
        List<ObjJProtocolDeclaration> out = new ArrayList<>();
        getAllInheritedProtocols(out, className, project);
        return out;
    }

    @NotNull
    public static void getAllInheritedProtocols(@NotNull List<ObjJProtocolDeclaration> out, @NotNull String className, @NotNull Project project) {
        ProgressIndicatorProvider.checkCanceled();
        if (className.equals(UNDETERMINED) || className.equals(ObjJClassType.CLASS) || ObjJClassType.isPrimitive(className)) {
            return;
        }

        if (isProtocolInArray(out, className)) {
            return;
        }

        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(project)) {
            return;
        }

        List<ObjJProtocolDeclaration> temp = ObjJProtocolDeclarationsIndex.getInstance().get(className, project);
        if (temp.isEmpty()) {
            return;
        }
        ObjJProtocolDeclaration thisProtocol = temp.get(0);
        out.add(thisProtocol);
        ObjJInheritedProtocolList protocolList = thisProtocol.getInheritedProtocolList();
        if (protocolList == null) {
            return;
        }
        for (ObjJClassName parentProtocolNameElement : protocolList.getClassNameList()) {
            ProgressIndicatorProvider.checkCanceled();
            getAllInheritedProtocols(out, parentProtocolNameElement.getText(), project);
            /*
            for (ObjJProtocolDeclaration currentProtocolInLoop: ObjJProtocolDeclarationsIndex.getInstance().get(parentProtocolNameElement.getText(), project)) {
                ProgressIndicatorProvider.checkCanceled();
                inheritedProtocolList = currentProtocolInLoop != null ? currentProtocolInLoop.getInheritedProtocolList() : null;
                if (inheritedProtocolList == null) {
                    continue;
                }
                for (ObjJClassName currentLoopClassName : inheritedProtocolList.getClassNameList()) {
                    getAllInheritedProtocols(out, currentLoopClassName.getText(), project);
                }
            }
            */
        }
    }

    private static boolean isProtocolInArray(@NotNull List<ObjJProtocolDeclaration> protocolDeclarations, @NotNull String className) {
        for (ObjJProtocolDeclaration protocolDeclaration : protocolDeclarations) {
            ProgressIndicatorProvider.checkCanceled();
            if (protocolDeclaration.getClassNameString().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static void getAllInheritedClasses(@NotNull List<String> classNames, @NotNull String className, @NotNull Project project) {
        if (className.equals(UNDETERMINED) || className.equals(ObjJClassType.CLASS) || ObjJClassType.isPrimitive(className)) {
            return;
        }

        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(project)) {
            classNames.add(ObjJClassType.UNDETERMINED);
            return;
        }
        Collection<ObjJClassDeclarationElement> classesDeclarations = ObjJClassDeclarationsIndex.getInstance().get(className, project);
        if (classesDeclarations.isEmpty()) {
            return;
        }
        if (!classNames.contains(className)) {
            classNames.add(className);
        }
        for (ObjJClassDeclarationElement classDeclaration : classesDeclarations) {
            ObjJClassDeclarationPsiUtil.addProtocols(classDeclaration, classNames);
            if (classDeclaration instanceof ObjJImplementationDeclaration) {
                String superClassName = ((ObjJImplementationDeclaration) classDeclaration).getSuperClassName();
                if (superClassName == null || classNames.contains(superClassName)) {
                    continue;
                }
                getAllInheritedClasses(classNames, superClassName, project);
            }
        }
    }

    public static boolean isSubclassOrSelf(@Nullable
                                                   String parentClass, @Nullable String subclassName, @NotNull Project project) throws CannotDetermineException {
        if (parentClass == null || subclassName == null) {
            return false;
        }
        if (parentClass.equals(subclassName)) {
            return true;
        }
        if (parentClass.equals(ObjJClassType.UNDEF_CLASS_NAME) || subclassName.equals(ObjJClassType.UNDEF_CLASS_NAME)) {
            throw new CannotDetermineException();
        }
        if (parentClass.equals(UNDETERMINED) || parentClass.equals(UNDETERMINED)) {
            return true;
        }
        return getAllInheritedClasses(subclassName, project).contains(parentClass);
    }


    public static void getAllInheritedClassesForAllClassTypesInArray(
            @NotNull
                    List<String> result,
            @NotNull
                    List<String> baseClassNames,
            @NotNull
                    Project project) {
        for (String baseClassName : baseClassNames) {
            getInheritedClasses(result, baseClassName, project);
        }
    }

    public static void getInheritedClasses(
            @NotNull
                    List<String> result,
            @NotNull
                    String baseClassName,
            @NotNull
                    Project project) {
        if (baseClassName.equals(ObjJClassType.CLASS)) {
            result.add(UNDETERMINED);
        }
        if (result.contains(baseClassName)) {
            return;
        }
        if (baseClassName.equals(ObjJClassType.JSOBJECT) && !result.contains(ObjJClassType.CPOBJECT)) {
            result.add(ObjJClassType.CPOBJECT);
        }
        for (String inheritedClassName : ObjJPsiImplUtil.getAllInheritedClasses(baseClassName, project)) {
            if (/*ObjJClassType.isPrimitive(inheritedClassName) || */result.contains(inheritedClassName)) {
                continue;
            }
            result.add(inheritedClassName);
        }
    }

    public static List<String> getInheritanceUpAndDown(@NotNull String className, @NotNull Project project) {
        List<String> referencedAncestors = new ArrayList<>();
        for (String parentClass : ObjJInheritanceUtil.getAllInheritedClasses(className, project)) {
            if (!referencedAncestors.contains(parentClass)) {
                referencedAncestors.add(parentClass);
            }
        }
        for (String childClass : ObjJClassInheritanceIndex.getInstance().getChildClassesAsStrings(className, project)) {
            if (!referencedAncestors.contains(childClass)) {
                referencedAncestors.add(childClass);
            }
        }
        return referencedAncestors;
    }

}
