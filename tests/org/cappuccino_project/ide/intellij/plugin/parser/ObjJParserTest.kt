package org.cappuccino_project.ide.intellij.plugin.parser

import cappuccino.ide.intellij.plugin.parser.ObjJParserDefinition
import com.intellij.testFramework.ParsingTestCase

class ObjJParserTest : ParsingTestCase("", "objj", ObjJParserDefinition()) {

    fun testParsingTestData() {
        doTest(true)
    }

    @Override
    override fun skipSpaces(): Boolean {
        return false
    }

    @Override
    override fun includeRanges(): Boolean {
        return true
    }
}
