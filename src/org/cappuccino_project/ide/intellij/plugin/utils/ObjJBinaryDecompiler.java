package org.cappuccino_project.ide.intellij.plugin.utils;

import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.cappuccino_project.decompiler.parser.ObjJSjLexer;
import org.cappuccino_project.decompiler.parser.ObjJSjListener;
import org.cappuccino_project.decompiler.parser.ObjJSjParser;
import org.cappuccino_project.decompiler.parser.ObjJSjParserListener;
import org.cappuccino_project.decompiler.templates.manual.FileTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ObjJBinaryDecompiler implements BinaryFileDecompiler {

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile virtualFile) {
        final ObjJSjLexer lexer;
        StringBuilder out = new StringBuilder();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(virtualFile.contentsToByteArray());
            lexer = new ObjJSjLexer(CharStreams.fromStream(inputStream));
        } catch (IOException ioe) {
            return out.append("Decompiler failed with error: <").append(ioe.getMessage()).append(">");
        }
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ObjJSjParser parser = new ObjJSjParser(tokenStream);
        ObjJSjParser.ScriptContext context = parser.script();
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        ObjJSjParserListener listener = new ObjJSjListener();
        parseTreeWalker.walk(listener, context);
        for (FileTemplate fileTemplate : ((ObjJSjListener) listener).getFileTemplates()) {
            fileTemplate.appendTo(out);
            out.append("\n");
        }
        return out;
    }
}
