package cappuccino.ide.intellij.plugin.contributor.utils

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project

import java.util.ArrayList
import java.util.HashMap

class SyncEditAction : AnAction("SyncEdit") {

    override fun update(e: AnActionEvent?) {
        val dataContext = e!!.dataContext
        var enabled = false
        val editor = dataContext.getData(DataKeys.EDITOR)
        if (editor != null) {
            val selectionModel = editor.selectionModel
            enabled = selectionModel.hasSelection()
        }
        val presentation = e.presentation
        presentation.isEnabled = enabled
    }

    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = event.dataContext
        val project = dataContext.getData(DataKeys.PROJECT)
        if (project == null) {
            LOGGER.error("Cannot perform sync edit action. Project is null")
            return
        }

        val templateManager = TemplateManager.getInstance(project)
        val editor = dataContext.getData(DataKeys.EDITOR) ?: return
        val selectionModel = editor.selectionModel
        if (selectionModel.hasSelection()) {
            val document = editor.document
            val isWritable = document.isWritable
            if (!isWritable) {
                return
            }
            val selectionStart = selectionModel.selectionStart
            val selectionEnd = selectionModel.selectionEnd
            val content = document.charsSequence
            val selection = content.subSequence(selectionStart, selectionEnd).toString()
            val words = splitIntoWords(selection)
            val countMap = countWords(words)
            val variables = getVariables(words, countMap)
            val text = joinWords(words)

            val tempTemplate = createTemplate(text, variables)

            templateManager.startTemplate(editor, "", tempTemplate)
        }
    }

    private fun createTemplate(text: String, variables: List<*>): TemplateImpl {
        val tempTemplate = TemplateImpl("", text, "other")

        tempTemplate.key = "hi"
        tempTemplate.description = "sync edit template"
        tempTemplate.isToIndent = false
        tempTemplate.isToReformat = false
        tempTemplate.isToShortenLongNames = false
        tempTemplate.parseSegments()
        tempTemplate.isInline = false

        val variableCount = variables.size
        for (i in 0 until variableCount) {
            val value = variables[i] as String
            val name = getVariableName(i)
            val quotedValue = '"'.toString() + value + '"'.toString()
            tempTemplate.addVariable(name, quotedValue, quotedValue, true)
        }
        return tempTemplate
    }

    companion object {
        private val LOGGER = Logger.getInstance(SyncEditAction::class.java.name)
        private val ONE = 1


        private fun getVariables(words: MutableList<String>, wordCountMap: Map<String, Int>): List<String> {
            val variables = ArrayList<String>()
            val wordCount = words.size
            val word2VariableIndex = HashMap<String, Int>()
            for (wordIndex in 0 until wordCount) {
                val word = words[wordIndex]
                if (isWordChar(word[0])) {
                    val count = wordCountMap[word] ?: continue
                    if (count > 1) {
                        val variableIndexInteger = word2VariableIndex[word]
                        val variableIndex: Int
                        if (variableIndexInteger != null) {
                            variableIndex = variableIndexInteger
                        } else {
                            variableIndex = variables.size
                            word2VariableIndex[word] = variableIndex
                            variables.add(word)
                        }
                        val variableName = getVariableName(variableIndex)
                        val variableExpression = '$'.toString() + variableName + '$'.toString()
                        words[wordIndex] = variableExpression
                    }
                }
            }
            return variables
        }

        private fun countWords(words: List<*>): Map<String, Int> {
            val map = HashMap<String, Int>()
            for (word1 in words) {
                val word = word1 as String
                if (isWordChar(word[0])) {
                    val countInteger = map[word]
                    if (countInteger == null) {
                        map[word] = ONE
                    } else {
                        map[word] = countInteger + 1
                    }
                }
            }
            return map
        }


        private fun splitIntoWords(text: String): MutableList<String> {
            val length = text.length
            val words = ArrayList<String>(length / 3)
            var wordStartIndex = 0
            var isWordChar = isWordChar(text[0])
            var i: Int
            i = 1
            while (i < length) {
                val newIsWordChar = isWordChar(text[i])
                if (isWordChar != newIsWordChar) {
                    val word = text.substring(wordStartIndex, i)
                    words.add(word)
                    wordStartIndex = i
                }
                isWordChar = newIsWordChar
                ++i
            }
            if (wordStartIndex != length) {
                val word = text.substring(wordStartIndex, i)
                words.add(word)
            }
            return words
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val words = splitIntoWords(" Ha 34lk bubuu ")
            for (o in words) {
                println(">$o<-")
            }
        }

        private fun isWordChar(c: Char): Boolean {
            return Character.isLetter(c) || Character.isDigit(c)
        }

        private fun joinWords(words: List<*>): String {
            val buffer = StringBuffer()
            for (word1 in words) {
                val word = word1 as String
                buffer.append(word)
            }
            return String(buffer)
        }

        private fun getVariableName(i: Int): String {
            return 'v' + Integer.toString(i)
        }
    }
}