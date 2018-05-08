package org.cappuccino_project.ide.intellij.plugin.parser

import com.intellij.testFramework.ParsingTestCase

class ObjJParserTest : ParsingTestCase("", "objj", ObjJParserDefinition()) {

    protected val testDataPath: String
        @Override
        get() = "code_samples/simple_language_plugin/testData"

    fun testParsingTestData() {
        doTest(true)
    }

    @Override
    protected fun skipSpaces(): Boolean {
        return false
    }

    @Override
    protected fun includeRanges(): Boolean {
        return true
    }
}
