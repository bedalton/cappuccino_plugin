package org.cappuccino_project.ide.intellij.plugin.contributor

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

import java.util.Arrays
import java.util.regex.Pattern

class ObjJNameValidator : NamesValidator {

    override fun isKeyword(
            string: String, project: Project): Boolean {
        return ObjJKeywordsList.keywords.indexOf(string) >= 0 || !validNamePattern.matcher(string).matches()
    }

    override fun isIdentifier(
            string: String, project: Project): Boolean {
        return validNamePattern.matcher(string).matches()
    }

    companion object {


        private val validNamePattern = Pattern.compile("(^[a-zA-Z0-9_$]*)$")
    }
}
