package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import java.util.ArrayList


class ExpressionReturnTypeResults private constructor(val references: MutableList<ExpressionReturnTypeReference>, private val project: Project) {
    private var changed = false
    private var referencedAncestors: MutableList<String>? = null

    val inheritanceUpAndDown: List<String>
        get() {
            if (referencedAncestors != null && !changed) {
                return referencedAncestors as MutableList<String>
            } else if (DumbService.isDumb(project)) {
                return ArrayUtils.EMPTY_STRING_ARRAY
            } else {
                referencedAncestors = ArrayList()
            }
            changed = false
            for (result in references) {
                if (ObjJClassType.isPrimitive(result.type)) {
                    continue
                }
                getInheritanceUpAndDown(referencedAncestors!!, result.type)
            }
            return referencedAncestors as ArrayList<String>
        }

    constructor(project: Project) : this(ArrayList<ExpressionReturnTypeReference>(), project)

    fun tick(refs: Set<String>) {
        for (ref in refs) {
            tick(ref)
        }
    }

    private fun tick(ref: String, ticksIn: Int) {
        var ticks = ticksIn

        var refObject = getReference(ref)
        if (refObject == null) {
            tick(ref)
            ticks -= 1
            refObject = getReference(ref)
        }
        assert(refObject != null)
        refObject!!.references += ticks
    }

    fun tick(results: ExpressionReturnTypeResults) {
        for (ref in results.references) {
            tick(ref.type, ref.references)
        }
    }

    fun tick(ref: String) {
        if (ref.isEmpty()) {
            return
        }
        var result = getReference(ref)
        if (result != null) {
            result.tick()
        } else {
            changed = true
            result = ExpressionReturnTypeReference(ref)
            references.add(result)
        }
    }

    fun getReference(ref: String): ExpressionReturnTypeReference? {
        if (ref.isEmpty()) {
            return null
        }
        for (result in references) {
            if (result.type == ref) {
                return result
            }
        }
        return null
    }

    private fun getInheritanceUpAndDown(referencedAncestors: MutableList<String>, className: String) {
        if (referencedAncestors.contains(className)) {
            return
        }
        for (currentClassName in ObjJInheritanceUtil.getInheritanceUpAndDown(className, project)) {
            if (!referencedAncestors.contains(currentClassName)) {
                referencedAncestors.add(currentClassName)
            }
        }
    }
}

class ExpressionReturnTypeReference internal constructor(val type: String) {
    var references: Int = 0
        internal set

    init {
        references = 1
    }

    internal fun tick(): Int {
        return ++references
    }
}