@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package cappuccino.ide.intellij.plugin.psi.types

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.exceptions.CannotDetermineException
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil

import java.util.*

data class ObjJClassTypeName(val className: String)

/**
 * A simple class declaring virtual class type references
 */
object ObjJClassType {

    const val ARRAY = "CPArray"
    const val CPDICTIONARY = "CPDictionary"
    const val ID = "id"
    const val BOOL = "BOOL"
    const val STRING = "CPString"
    const val INT = "int"
    const val CPINT = "CPInteger"
    const val CPUINT = "CPUInteger"
    const val UNSIGNED = "unsigned"
    const val SIGNED = "signed"
    const val CHAR = "char"
    const val SHORT = "short"
    const val BYTE = "byte"
    const val LONG = "long"
    const val FLOAT = "float"
    const val DOUBLE = "double"
    @Deprecated("")
    val SELECTOR = "selector"
    const val FUNCTION = "Function"
    const val REGEX = "regex"
    const val UNDETERMINED = "UNDEF"
    const val CPOBJECT = "CPObject"
    const val JSOBJECT = "JSObject"
    const val CLASS = "Class"
    const val CPAPP = "CPApp"
    const val CPAPPLICATION = "CPApplication"
    const val NIL = "NIL"
    const val JS_FUNCTION = "func"
    const val SEL = "SEL"
    const val PROTOCOL = "Protocol"
    const val AT_ACTION = "@action"
    const val CG_RECT = "CGRect"
    const val CG_SIZE = "CGSize"
    const val CG_POINT = "CGPoint"
    const val CG_IMAGE = "CGImage"
    const val DOM_ELEMENT = "DOMElement"
    const val OBJECT_LIT = "Object"
    val PRIMITIVE_VAR_NAMES = listOf(ID, SHORT, LONG, UNSIGNED, SIGNED, BYTE, CHAR, DOUBLE, BOOL, FLOAT, INT)
    val ADDITIONAL_PREDEFINED_CLASSES = listOf(
            ID,
            SHORT,
            LONG,
            UNSIGNED,
            SIGNED,
            BYTE,
            CHAR,
            DOUBLE,
            BOOL,
            FLOAT,
            INT,
            STRING,
            CPINT,
            CPUINT,
            FUNCTION,
            REGEX,
            CPOBJECT,
            JSOBJECT,
            CLASS,
            CPAPP,
            CPAPPLICATION,
            CPDICTIONARY,
            SEL,
            PROTOCOL,
            CG_RECT,
            CG_SIZE,
            DOM_ELEMENT,
            JS_FUNCTION,
            CG_POINT,
            CG_IMAGE,
            OBJECT_LIT)

    private val INT_EQUIVALENTS = Arrays.asList(INT, UNSIGNED, SIGNED, CHAR, SHORT, BYTE)
    private val LONG_EQUIVALENTS = mutableListOf<String>()
    private val FLOAT_EQUIVALENTS = mutableListOf<String>()
    private val DOUBLE_EQUIVALENTS = mutableListOf<String>()
    private val NUMERIC_TYPES = mutableListOf<String>()

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
    private val classTypes = HashMap<String, ObjJClassTypeName>()
    const val UNDEF_CLASS_NAME = "{UNDEF}"
    val UNDEF = ObjJClassType.getClassType(UNDEF_CLASS_NAME)
    const val VOID_CLASS_NAME = "void"
    val VOID = ObjJClassType.getClassType(VOID_CLASS_NAME)

    /**
     * Static class type getter, used to ensure that only one version of class type exists for class name
     * May be unnecessary as class type has overriding equals method that checks a given class name equality
     */
    @Synchronized
    fun getClassType(className: String?): ObjJClassTypeName {
        if (className == null) {
            return ObjJClassType.UNDEF
        }
        if (classTypes.containsKey(className)) {
            val classType = classTypes[className]
            if (classType != null) {
                return classType
            }
        }
        val classType = ObjJClassTypeName(className)
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