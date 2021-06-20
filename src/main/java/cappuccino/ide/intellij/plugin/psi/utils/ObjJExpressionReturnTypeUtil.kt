package cappuccino.ide.intellij.plugin.psi.utils


class MixedReturnTypeException internal constructor(val returnTypesList: List<String>) : Exception("More than one return type found")
