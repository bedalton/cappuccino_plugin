package cappuccino.ide.intellij.plugin.jstypedef.contributor

object JsPrimitives {
    val primitives = listOf(
            "string", "String",
            "boolean", "BOOL", "Boolean",
            "int", "Int",
            "number", "Number",
            "double", "Double",
            "float", "Float",
            "long", "Long",
            "char", "Char",
            "byte", "Byte",
            "short", "short",
            "null", "nil", "Nil", "undefined"
    )

    private val primitivesLowerCase = primitives.map { it.toLowerCase() }.toSet()

    fun isPrimitive(type:String): Boolean {
        return type.toLowerCase() in primitivesLowerCase
    }
}