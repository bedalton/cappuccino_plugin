package cappuccino.ide.intellij.plugin.psi.types

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.exceptions.CannotDetermineException
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIsOfClassType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil

import java.util.*

/**
 * A simple class declaring virtual class type references
 */
class ObjJClassType
/**
 * Constructs a class type using a class name string
 * @param className class name as string
 */
private constructor(//Class name as string
        /**
         * Gets the class name as string
         * @return class name as string
         */
        val className: String) {

    /**
     * Checks whether this class type has given class name
     * @param className class name
     * @return `true` if class names match, `false` otherwise
     */
    fun hasClassName(className: String): Boolean {
        return className == this.className
    }

    /**
     * Checks whether two class types are considered references to the same place
     * @param classType class type
     * @return `true` if class types are considered equal. `false` otherwise
     */
    fun isClassType(classType: ObjJClassType?): Boolean {
        return classType != null && hasClassName(classType.className)
    }

    /**
     * Overrides class to check if two classes are equal
     * @param object object to check equality for
     * @return `true` if considered equal, `false otherwise`
     */
    override fun equals(`object`: Any?): Boolean {
        //Object is null, and not equal
        if (`object` == null) {
            return false
        }

        // Actually points to exact same object
        if (`object` === this) {
            return true
        }
        val classType: ObjJClassType?
        // Object of class is of ClassType
        if (`object` is ObjJClassType) {
            classType = `object`
            // Object is instance of IsClass Type
            // Get class type referenced by interface
        } else if (`object` is ObjJIsOfClassType) {
            classType = `object`.classType
            // Object does not have known reference to a class type
        } else {
            classType = null
        }
        //Returns true if a class type is found, and if they are equal
        return classType != null && isClassType(classType)
    }

    companion object {

        val ARRAY = "CPArray"
        val OBJECT = "CPDictionary"
        val ID = "id"
        val BOOL = "BOOL"
        val STRING = "CPString"
        val INT = "int"
        val UNSIGNED = "unsigned"
        val SIGNED = "signed"
        val CHAR = "char"
        val SHORT = "short"
        val BYTE = "byte"
        val LONG = "long"
        val FLOAT = "float"
        val DOUBLE = "double"
        @Deprecated("")
        val SELECTOR = "selector"
        val FUNCTION = "function"
        val REGEX = "regex"
        val UNDETERMINED = "id"
        val CPOBJECT = "CPObject"
        val JSOBJECT = "JSObject"
        val CLASS = "Class"
        val CPAPP = "CPApp"
        val CPAPPLICATION = "CPApplication"
        val NIL = "NIL"
        val JS_FUNCTION = "func"
        val SEL = "SEL"
        val PROTOCOL = "Protocol"
        val AT_ACTION = "@action"

        private val INT_EQUIVALENTS = Arrays.asList(INT, UNSIGNED, SIGNED, CHAR, SHORT, BYTE)
        private val LONG_EQUIVALENTS = ArrayList<String>()
        private val FLOAT_EQUIVALENTS = ArrayList<String>()
        private val DOUBLE_EQUIVALENTS = ArrayList<String>()
        private val NUMERIC_TYPES = ArrayList<String>()

        init {
            LONG_EQUIVALENTS.add(LONG)
            LONG_EQUIVALENTS.addAll(INT_EQUIVALENTS)

            FLOAT_EQUIVALENTS.add(FLOAT)
            FLOAT_EQUIVALENTS.addAll(LONG_EQUIVALENTS)

            DOUBLE_EQUIVALENTS.add(DOUBLE)
            DOUBLE_EQUIVALENTS.addAll(FLOAT_EQUIVALENTS)
            NUMERIC_TYPES.addAll(DOUBLE_EQUIVALENTS)
        }


        //Container of existing class types
        private val classTypes = HashMap<String, ObjJClassType>()
        val UNDEF_CLASS_NAME = "{UNDEF}"
        val UNDEF = ObjJClassType.getClassType(UNDEF_CLASS_NAME)
        val VOID_CLASS_NAME = "VOID"
        val VOID = ObjJClassType.getClassType(VOID_CLASS_NAME)

        val PRIMITIVE_VAR_NAMES = Arrays.asList(ID, SHORT, LONG, UNSIGNED, SIGNED, BYTE, CHAR, DOUBLE, BOOL, FLOAT)

        /**
         * Static class type getter, used to ensure that only one version of class type exists for class name
         * May be unnecessary as class type has overriding equals method that checks a given class name equality
         */
        @Synchronized
        fun getClassType(className: String?): ObjJClassType {
            if (className == null) {
                return ObjJClassType.UNDEF
            }
            if (classTypes.containsKey(className)) {
                val classType = classTypes[className]
                if (classType != null) {
                    return classType
                }
            }
            val classType = ObjJClassType(className)
            classTypes[className] = classType
            return classType
        }

        @Throws(IndexNotReadyInterruptingException::class, CannotDetermineException::class)
        fun isSubclassOrSelf(parentClass: String, subclass: String, project: Project): Boolean {
            if (parentClass == subclass) {
                return true
            }
            if (INT_EQUIVALENTS.contains(parentClass)) {
                return INT_EQUIVALENTS.contains(subclass)
            }

            if (parentClass == LONG) {
                return LONG_EQUIVALENTS.contains(subclass)
            }

            if (parentClass == FLOAT) {
                return FLOAT_EQUIVALENTS.contains(subclass)
            }
            if (parentClass == DOUBLE) {
                return DOUBLE_EQUIVALENTS.contains(subclass)
            }
            if (parentClass == ID) {
                return !isPrimitive(subclass)
            }
            if (subclass == ID) {
                return !isPrimitive(parentClass)
            }

            if (parentClass == PROTOCOL) {
                return !isPrimitive(subclass)
            }

            return if (DumbService.isDumb(project)) {
                false
            } else ObjJInheritanceUtil.isSubclassOrSelf(parentClass, subclass, project)
        }

        fun isPrimitive(className: String): Boolean {
            return NUMERIC_TYPES.contains(className) ||
                    className == BOOL ||
                    className == SEL
        }
    }

}