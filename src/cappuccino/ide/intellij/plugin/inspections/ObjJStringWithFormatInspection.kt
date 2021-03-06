package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJRemoveTrailingStringFormatParameter
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferMethodCallType
import cappuccino.ide.intellij.plugin.inference.stringTypes
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import java.util.*
import java.util.regex.Pattern

class ObjJStringWithFormatInspection : LocalInspectionTool() {


    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodCall(objJMethodCall: ObjJMethodCall) {
                super.visitMethodCall(objJMethodCall)
                annotateStringWithFormat(objJMethodCall, problemsHolder)
            }
        }
    }


    companion object {

        private const val CPSTRING_INIT_WITH_FORMAT = "initWithFormat"
        private const val CPSTRING_STRING_WITH_FORMAT = "stringWithFormat"

        /**
         * Validates and annotates CPString formatter
         * Simply checks that there are enough arguments in the list for all wildcards in string format
         * @param methodCall method call
         * @param problemsHolder annotation holder
         */
        private fun annotateStringWithFormat(methodCall: ObjJMethodCall, problemsHolder: ProblemsHolder) {
            if (!isCPStringWithFormat(methodCall)) {
                return
            }
            if (methodCall.qualifiedMethodCallSelectorList.isEmpty()) {
                return
            }
            val expressions = methodCall.qualifiedMethodCallSelectorList[0].exprList
            if (expressions.size < 1) {
                problemsHolder.registerProblem(methodCall, ObjJBundle.message("objective-j.inspection.string-format.first-parameter-must-be-string.message"))
                return
            }
            val format = expressions.removeAt(0)
            /*val formatVariableType: String = try {
                getReturnType(format, true)
            } catch (e: MixedReturnTypeException) {
                e.returnTypesList[0]
            } ?: return

            if (!isUniversalMethodCaller(formatVariableType) && formatVariableType != ObjJClassType.STRING) {
                problemsHolder.registerProblem(format, "First parameter should be of type CPString")
                return
            }*/
            if (format.leftExpr == null || format.leftExpr!!.primary == null || format.leftExpr!!.primary!!.stringLiteral == null) {
                //   //LOGGER.info("[CPString initWithFormat] should have string expression first, but does not. Actual text: <"+format.getText()+">");
                return
            }
            val formatString = format.leftExpr!!.primary!!.stringLiteral!!.text
            val pattern = Pattern.compile("%([^%])*")
            val matchResult = pattern.matcher(formatString)
            val matches = ArrayList<String>()
            while (matchResult.find()) {
                matches.add(matchResult.group())
            }
            val numMatches = matches.size
            val numExpressions = expressions.size
            if (numMatches > numExpressions) {
                val formatStringOffsetInMethodCall = format.leftExpr!!.primary!!.stringLiteral!!.textRange.startOffset - methodCall.textRange.startOffset
                val parts = formatString.split("%([^%])".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var part: String
                val builder = StringBuilder()
                var offset: Int
                val lastIndex = parts.size - if (formatString.lastIndexOf("%") == formatString.length - 2 && formatString.lastIndexOf("%") != formatString.length - 1) 0 else 1
                for (i in 0 until lastIndex) {
                    part = parts[i]
                    builder.append(part)
                    offset = formatStringOffsetInMethodCall + builder.length
                    builder.append("%?")
                    if (i < numExpressions) {
                        continue
                    }
                    ////LOGGER.info("Current substring = <"+builder.toString()+">");
                    problemsHolder.registerProblem(methodCall, TextRange.create(offset, offset + 2), ObjJBundle.message("objective-j.inspection.string-format.not-enough-values.message", numMatches, numExpressions))
                }
            } else if (numMatches < numExpressions) {
                for (i in numMatches until numExpressions) {
                    problemsHolder.registerProblem(expressions[i], ObjJBundle.message("objective-j.inspection.string-format.too-many-values.message", numMatches, numExpressions), ObjJRemoveTrailingStringFormatParameter(expressions[i]))
                }
            }
            /*
            for (i in 1..numMatches) {
                if (expressions.size < 1) {
                    break
                }
                val expr = expressions.removeAt(0)
                //TODO check var type for match
            }*/
        }

        /**
         * Checks whether method call is a CPString format method call.
         * @param methodCall method call
         * @return true if method call is string formatting method call, false otherwise.
         */
        private fun isCPStringWithFormat(methodCall: ObjJMethodCall): Boolean {
            if (methodCall.selectorList.size == 1) {
                val selectorText = methodCall.selectorList.getOrNull(0)?.text ?: return false
                if (selectorText != CPSTRING_INIT_WITH_FORMAT && selectorText != CPSTRING_STRING_WITH_FORMAT)
                    return false
                if (methodCall.callTargetText == ObjJClassType.STRING)
                    return true
                val inferredTypes = inferMethodCallType(methodCall, createTag())?.toClassList("?") ?: return true
                return inferredTypes.isEmpty() || inferredTypes.any {
                    it == "?" || it.toLowerCase() in stringTypes
                }
            }
            return false
        }
    }
}
