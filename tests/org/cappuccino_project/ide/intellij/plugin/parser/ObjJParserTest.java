package org.cappuccino_project.ide.intellij.plugin.parser;

import com.intellij.testFramework.ParsingTestCase;

public class ObjJParserTest  extends ParsingTestCase {
    public ObjJParserTest() {
        super("", "objj", new ObjJParserDefinition());
    }

    public void testParsingTestData() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "code_samples/simple_language_plugin/testData";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}
