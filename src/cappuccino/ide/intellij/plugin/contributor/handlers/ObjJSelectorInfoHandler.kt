package cappuccino.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*

class ObjJSelectorInfoHandler : ParameterInfoHandler<Any, Any> {
    override fun couldShowInLookup(): Boolean {
        return false
    }

    override fun getParametersForLookup(lookupElement: LookupElement, parameterInfoContext: ParameterInfoContext): Array<Any>? {
        return arrayOf(0)
    }

    override fun findElementForParameterInfo(
            createParameterInfoContext: CreateParameterInfoContext): Any? {
        return null
    }

    override fun showParameterInfo(
            o: Any,
            createParameterInfoContext: CreateParameterInfoContext) {

    }

    override fun findElementForUpdatingParameterInfo(
            updateParameterInfoContext: UpdateParameterInfoContext): Any? {
        return null
    }

    override fun updateParameterInfo(
            o: Any,
            updateParameterInfoContext: UpdateParameterInfoContext) {

    }

    override fun updateUI(o: Any,
                          parameterInfoUIContext: ParameterInfoUIContext) {

    }
}
