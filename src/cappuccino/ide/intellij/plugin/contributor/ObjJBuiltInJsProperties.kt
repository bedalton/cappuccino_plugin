@file:Suppress("MemberVisibilityCanBePrivate")

package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.BOOL
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.INT
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.STRING
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED

object ObjJBuiltInJsProperties {

    val values = listOf("Infinity", "NaN", "null", "undefined")

    private val window = property(".") {

        prop("Infinity") {
            desc = " numeric value representing infinity"
            rtype = INT
        }
        prop("NaN") {
            desc = "value representing Not-A-Number"
            rtype = INT
        }
        prop("null") {
            rtype = ObjJClassType.NIL
        }
        prop("undefined") {
            rtype = ObjJClassType.NIL
        }

        cfunc("eval") {
            desc = "evaluates JavaScript code represented as a string"
            rtype = UNDETERMINED
        }
        cfunc("uneval") {
            rtype = STRING
            desc = "creates a string representation of the source code of an Object"
            warn = "This feature is non-standard and is not on a standards track. Do not use it on production sites facing the Web: it will not work for every user"
        }
        cfunc("isFinite") {
            rtype = BOOL
            desc = "determines whether the passed value is a finite number"
        }
        cfunc("parseInt") {
            rtype = INT
        }
        cfunc("decodeURI") {
            rtype = STRING
        }
        cfunc("decodeURIComponent") {
            rtype = STRING
        }
        cfunc("encodeURI") {
            rtype = STRING
        }
        cfunc("encodeURIComponent") {
            rtype = STRING
        }
        cfunc("escape") {
            rtype = STRING
        }
        cfunc("unescape") {
            rtype = STRING
        }

        prop("Object") {
            cfunc("assign") {
                desc = "Copies the values of all enumerable own properties from one or more source objects to a target object."
            }
            cfunc("create") {
                rtype = "Object"
                desc = "Creates a new object with the specified prototype object and properties.\n"
            }
            cfunc("defineProperty")
            cfunc("defineProperties")
            cfunc("entries")
            cfunc("freeze")
            cfunc("getOwnPropertyDescriptor")
            cfunc("getOwnPropertyDescriptors")
            cfunc("getOwnPropertyNames")
            cfunc("getOwnPropertySymbols")
            cfunc("getPrototypeOf")
            cfunc("is")
            cfunc("isExtensible")
            cfunc("isFrozen")
            cfunc("isSealed()")
            cfunc("keys()")
            cfunc("preventExtensions()")
            cfunc("seal()")
            cfunc("setPrototypeOf()")
            cfunc("values()")
            pfunc("hasOwnProperty()")
            pfunc("isPrototypeOf()")
            pfunc("propertyIsEnumerable()")
            pfunc("toSource() ") {
                warn = "This feature is non-standard and is not on a standards track. Do not use it on production sites facing the Web: it will not work for every user"
            }
            pfunc("toLocaleString()")
            pfunc("toString()")
            pfunc("valueOf()")
        }
        prop("Function") {
            cfunc("length")
            cfunc("name")
            pfunc("apply")
            pfunc("find")
            pfunc("bind")
            pfunc("call")
            pfunc("toString")
        }
        cfunc("Boolean")
        cfunc("Symbol")
        prop("Error")
        prop("EvalError") {
            extends = "Error"
        }
        prop("InternalError") {
            extends = "Error"
        }
        prop("RangeError") {
            extends = "Error"
        }
        prop("ReferenceError") {
            extends = "Error"
        }
        prop("SyntaxError") {
            extends = "Error"
        }
        prop("TypeError") {
            extends = "Error"
        }
        prop("URIError") {
            extends = "Error"
        }
        prop("Number")
        prop("Math")
        prop("Date")
        prop("String")
        prop("RegExp")
        prop("Array")
        prop("Int8Array")
        prop("Uint8Array")
        prop("Uint8ClampedArray")
        prop("Int16Array")
        prop("Uint16Array")
        prop("Int32Array")
        prop("Uint32Array")
        prop("Float32Array")
        prop("Float64Array")
        prop("Map")
        prop("Set")
        prop("WeakMap")
        prop("WeakSet")
        prop("SIMD")
        prop("ArrayBuffer")
        prop("SharedArrayBuffer")
        prop("Atomics")
        prop("DataView")
        prop("JSON")
        prop("Intl")
        prop("WebAssembly")
        prop("document")
        prop("window")
        prop("console")
    }

    fun propertyExists(property:String) : Boolean {
        for(prop in window.children) {
            if (prop.name == property) {
                return true
            }
        }
        return false
    }

    fun funcExists(property:String) : Boolean {
        for(prop in window.classFunctions) {
            if (prop.name == property) {
                return true
            }
        }
        for (prop in window.prototypeMethods) {
            if (prop.name == property) {
                return true
            }
        }
        return false
    }

    fun property (name:String, block:JsProperty.()->Unit) : JsProperty = JsProperty(name).apply(block)

    class JsProperty internal constructor(val name:String) {
        val children = arrayListOf<JsProperty>()
        val params = arrayListOf<String>()
        val classFunctions = arrayListOf<JsProperty>()
        val prototypeMethods = arrayListOf<JsProperty>()
        var rtype:String? = null
        var desc:String? = null
        var warn:String? = null
        var extends:String? = null

        fun param(name:String) = params.add(name)

        //Properties
        fun prop(name:String) = children.add(JsProperty(name))
        fun prop(name:String, init:JsProperty.()->Unit) = initProperty(JsProperty(name), init)
        private fun initProperty(property:JsProperty, init:JsProperty.()->Unit) : JsProperty {
            property.init()
            children.add(property)
            return property
        }

        // Class Function
        fun cfunc(name:String) = classFunctions.add(JsProperty(name))
        fun cfunc(name:String, init:JsProperty.()->Unit) = initFunction(JsProperty(name), init)
        private fun initFunction(property:JsProperty, init:JsProperty.()->Unit) : JsProperty {
            property.init()
            classFunctions.add(property)
            return property
        }

        //ProtoType Methods
        fun pfunc(name:String) = prototypeMethods.add(JsProperty(name))
        fun pfunc(name:String, init:JsProperty.()->Unit) = initPrototypeMethod(JsProperty(name), init)
        private fun initPrototypeMethod(property:JsProperty, init:JsProperty.()->Unit) : JsProperty {
            property.init()
            prototypeMethods.add(property)
            return property
        }
    }


}
