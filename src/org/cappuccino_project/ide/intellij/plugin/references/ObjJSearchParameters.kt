// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// Taken from https://github.com/JetBrains/intellij-community/blob/master/platform/indexing-api/src/com/intellij/psi/search/searches/DefinitionsScopedSearch.java
package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.QueryParameters

class ObjJSearchParameters @JvmOverloads constructor(val element: PsiElement, val scope: SearchScope = ReadAction.compute<SearchScope, RuntimeException>(ThrowableComputable<SearchScope, RuntimeException> { element.useScope }), val isCheckDeep: Boolean = true) : QueryParameters {
    private val myProject: Project

    init {
        myProject = PsiUtilCore.getProjectInReadAction(this.element)
    }

    override fun getProject(): Project? {
        return myProject
    }

    override fun isQueryValid(): Boolean {
        return element.isValid
    }
}
