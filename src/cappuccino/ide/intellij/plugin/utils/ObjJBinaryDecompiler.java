package cappuccino.ide.intellij.plugin.utils;

import cappuccino.decompiler.parser.ObjJSjLexer;
import cappuccino.decompiler.parser.ObjJSjListener;
import cappuccino.decompiler.parser.ObjJSjParser;
import cappuccino.decompiler.parser.ObjJSjParserListener;
import cappuccino.decompiler.templates.manual.FileTemplate;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
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
