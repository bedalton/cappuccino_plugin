package cappuccino.ide.intellij.plugin.annotator

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Contract

class AnnotationHolderWrapper(private val annotationHolder: AnnotationHolder) {

    @Contract(pure = true)
    fun newAnnotation(severity:HighlightSeverity, message: String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, severity, message)
    }

    @Contract(pure = true)
    fun newErrorAnnotation(message:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.ERROR, message)
    }

    @Contract(pure = true)
    fun newWarningAnnotation(message:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.WARNING, message)
    }

    @Contract(pure = true)
    fun newWeakWarningAnnotation(message:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.WEAK_WARNING, message)
    }

    @Contract(pure = true)
    fun newInfoAnnotation(message:String?) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.INFORMATION, message)
    }
}

class AnnotationBuilder private constructor(private val annotationBuilder: com.intellij.lang.annotation.AnnotationBuilder) {

    constructor(annotationHolder:AnnotationHolder, severity:HighlightSeverity, message: String?)
            : this(annotationHolder.newAnnotation(severity, message ?: ""))

    @Contract(pure = true)
    fun range(range:TextRange) : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.range(range))
    }

    @Contract(pure = true)
    fun range(element: PsiElement) : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.range(element))
    }

    @Contract(pure = true)
    fun withFix(fix:IntentionAction) : AnnotationBuilder  {
        return AnnotationBuilder(annotationBuilder.withFix(fix))
    }

    @Contract(pure = true)
    fun withFixes(fixes:List<IntentionAction>) : AnnotationBuilder {
        var builder:AnnotationBuilder = this
        fixes.forEach { fix ->
            builder = AnnotationBuilder(builder.annotationBuilder.withFix(fix))
        }
        return builder
    }

    @Contract(pure = true)
    fun needsUpdateOnTyping(needsUpdateOnTyping:Boolean) : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.needsUpdateOnTyping(needsUpdateOnTyping))
    }

    @Contract(pure = true)
    fun needsUpdateOnTyping() : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.needsUpdateOnTyping())
    }

    @Contract(pure = true)
    fun highlightType(highlightType:ProblemHighlightType) : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.highlightType(highlightType))
    }

    @Contract(pure = true)
    fun tooltip(tooltip:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.tooltip(tooltip))
    }

    @Contract(pure = true)
    fun textAttributes(textAttributes: TextAttributesKey) : AnnotationBuilder {
        return AnnotationBuilder(this.annotationBuilder.textAttributes(textAttributes))
    }

    @Contract(pure = true)
    fun enforcedTextAttributes(textAttributes:TextAttributes) : AnnotationBuilder {
        return AnnotationBuilder(annotationBuilder.enforcedTextAttributes(textAttributes))
    }

    @Contract(pure = true)
    fun newFix(intentionAction: IntentionAction): FixBuilder {
        return FixBuilder._createFixBuilder(annotationBuilder, annotationBuilder.newFix(intentionAction))
    }

    @Contract(pure = true)
    fun newLocalQuickFix(quickFix: LocalQuickFix, problemDescriptor: ProblemDescriptor): FixBuilder {
        return FixBuilder._createFixBuilder(annotationBuilder, annotationBuilder.newLocalQuickFix(quickFix, problemDescriptor))
    }

    @Contract(pure = true)
    fun create() {
        annotationBuilder.create()
    }

    class FixBuilder private  constructor(private val annotationBuilder: com.intellij.lang.annotation.AnnotationBuilder, private val fixBuilder: com.intellij.lang.annotation.AnnotationBuilder.FixBuilder) {

        @Contract(pure = true)
        fun range(range: TextRange): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilder.range(range))
        }

        @Contract(pure = true)
        fun key(key: HighlightDisplayKey): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilder.key(key))
        }

        @Contract(pure = true)
        fun batch(): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilder.batch())
        }

        @Contract(pure = true)
        fun universal(): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilder.universal())
        }

        @Contract(pure = true)
        fun registerFix(): AnnotationBuilder {
            return AnnotationBuilder(fixBuilder.registerFix())
        }

        companion object {
            @Suppress("FunctionName")
            internal fun _createFixBuilder(annotationBuilder: com.intellij.lang.annotation.AnnotationBuilder, fixBuilder: com.intellij.lang.annotation.AnnotationBuilder.FixBuilder) : FixBuilder {
                return FixBuilder(annotationBuilder, fixBuilder)
            }
        }
    }
}

