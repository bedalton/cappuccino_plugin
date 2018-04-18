package org.cappuccino_project.ide.intellij.plugin.psi.types;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.cappuccino_project.ide.intellij.plugin.exceptions.CannotDetermineException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJIsOfClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJClassDeclarationPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A simple class declaring virtual class type references
 */
public class ObjJClassType {

    public static final String ARRAY = "CPArray";
    public static final String OBJECT = "CPDictionary";
    public static final String ID = "id";
    public static final String BOOL = "BOOL";
    public static final String STRING = "CPString";
    public static final String INT = "int";
    public static final String UNSIGNED = "unsigned";
    public static final String SIGNED = "signed";
    public static final String CHAR = "char";
    public static final String SHORT = "short";
    public static final String BYTE = "byte";
    public static final String LONG = "long";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    @Deprecated
    public static final String SELECTOR = "selector";
    public static final String FUNCTION = "function";
    public static final String REGEX = "regex";
    public static final String UNDETERMINED = "{UNDETERMINED}";
    public static final String CPOBJECT = "CPObject";
    public static final String JSOBJECT = "JSObject";
    public static final String CLASS = "Class";
    public static final String CPAPP = "CPApp";
    public static final String CPAPPLICATION = "CPApplication";
    public static final String NIL = "NIL";
    public static final String JS_FUNCTION = "func";
    public static final String SEL = "SEL";
    public static final String PROTOCOL = "Protocol";
    public static final String AT_ACTION = "@action";

    private static final List<String> INT_EQUIVALENTS = Arrays.asList(INT,UNSIGNED,SIGNED,CHAR,SHORT, BYTE);
    private static final List<String> LONG_EQUIVALENTS = new ArrayList<>();
    private static final List<String> FLOAT_EQUIVALENTS = new ArrayList<>();
    private static final List<String> DOUBLE_EQUIVALENTS = new ArrayList<>();
    private static final List<String> NUMERIC_TYPES = new ArrayList<>();

    static {
        LONG_EQUIVALENTS.add(LONG);
        LONG_EQUIVALENTS.addAll(INT_EQUIVALENTS);

        FLOAT_EQUIVALENTS.add(FLOAT);
        FLOAT_EQUIVALENTS.addAll(LONG_EQUIVALENTS);

        DOUBLE_EQUIVALENTS.add(DOUBLE);
        DOUBLE_EQUIVALENTS.addAll(FLOAT_EQUIVALENTS);
        NUMERIC_TYPES.addAll(DOUBLE_EQUIVALENTS);
    }


    //Container of existing class types
    private static final HashMap<String, ObjJClassType> classTypes = new HashMap<>();
    //Class name as string
    private final String className;
    public static final String UNDEF_CLASS_NAME = "{UNDEF}";
    public static final ObjJClassType UNDEF = ObjJClassType.getClassType(UNDEF_CLASS_NAME);
    public static final String VOID_CLASS_NAME = "VOID";
    public static final ObjJClassType VOID = ObjJClassType.getClassType(VOID_CLASS_NAME);

    public static final List<String> PRIMITIVE_VAR_NAMES = Arrays.asList(ID, SHORT, LONG, UNSIGNED, SIGNED, BYTE, CHAR, DOUBLE, BOOL, FLOAT);


    /**
     * Constructs a class type using a class name string
     * @param className class name as string
     */
    private ObjJClassType(@NotNull String className) {
        this.className = className;
    }

    /**
     * Static class type getter, used to ensure that only one version of class type exists for class name
     * May be unnecessary as class type has overriding equals method that checks a given class name equality
     */
    public synchronized static ObjJClassType getClassType(String className) {
        if (classTypes.containsKey(className)) {
            final ObjJClassType classType = classTypes.get(className);
            if (classType != null) {
                return classType;
            }
        }
        final ObjJClassType classType = new ObjJClassType(className);
        classTypes.put(className, classType);
        return classType;
    }

    /**
     * Checks whether this class type has given class name
     * @param className class name
     * @return <code>true</code> if class names match, <code>false</code> otherwise
     */
    public boolean hasClassName(String className) {
        return Objects.equals(className, this.className);
    }

    /**
     * Checks whether two class types are considered references to the same place
     * @param classType class type
     * @return <code>true</code> if class types are considered equal. <code>false</code> otherwise
     */
    public boolean isClassType(@Nullable
                                       ObjJClassType classType) {
        return classType != null && hasClassName(classType.getClassName());
    }

    /**
     * Gets the class name as string
     * @return class name as string
     */
    public String getClassName() {
        return className;
    }

    /**
     * Overrides class to check if two classes are equal
     * @param object object to check equality for
     * @return <code>true</code> if considered equal, <code>false otherwise</code>
     */
    public boolean equals(Object object) {
        //Object is null, and not equal
        if (object == null) {
            return false;
        }

        // Actually points to exact same object
        if (object == this) {
            return true;
        }
        ObjJClassType classType;
        // Object of class is of ClassType
        if (object instanceof ObjJClassType) {
            classType = (ObjJClassType)object;
        // Object is instance of IsClass Type
        // Get class type referenced by interface
        } else if (object instanceof ObjJIsOfClassType) {
            final ObjJIsOfClassType isOfClassType = ((ObjJIsOfClassType)object);
            classType = isOfClassType.getClassType();
        // Object does not have known reference to a class type
        } else {
            classType = null;
        }
        //Returns true if a class type is found, and if they are equal
        return classType != null && isClassType(classType);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isSubclassOrSelf(@NotNull String parentClass, @NotNull String subclass, @NotNull Project project) throws IndexNotReadyInterruptingException, CannotDetermineException {
        if (parentClass.equals(subclass)) {
            return true;
        }
        if (INT_EQUIVALENTS.contains(parentClass)) {
            return INT_EQUIVALENTS.contains(subclass);
        }

        if (parentClass.equals(LONG)) {
            return LONG_EQUIVALENTS.contains(subclass);
        }

        if (parentClass.equals(FLOAT)) {
            return FLOAT_EQUIVALENTS.contains(subclass);
        }
        if (parentClass.equals(DOUBLE)) {
            return DOUBLE_EQUIVALENTS.contains(subclass);
        }
        if (parentClass.equals(ID)) {
            return !isPrimitive(subclass);
        }
        if (subclass.equals(ID)) {
            return !isPrimitive(parentClass);
        }

        if (parentClass.equals(PROTOCOL)) {
            return !isPrimitive(subclass);
        }

        if (DumbService.isDumb(project)) {
            throw new IndexNotReadyInterruptingException();
        }
        return ObjJInheritanceUtil.isSubclassOrSelf(parentClass, subclass, project);
    }

    public static boolean isPrimitive(@NotNull String className) {
        return  NUMERIC_TYPES.contains(className) ||
                className.equals(BOOL) ||
                className.equals(SEL);
    }

}
