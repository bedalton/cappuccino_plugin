package cappuccino.ide.intellij.plugin.decompiler.decompiler;

import cappuccino.decompiler.parser.ObjJSjLexer;
import cappuccino.decompiler.parser.ObjJSjListener;
import cappuccino.decompiler.parser.ObjJSjParser;
import cappuccino.decompiler.parser.ObjJSjParserListener;
import cappuccino.decompiler.templates.manual.FileTemplate;
import cappuccino.ide.intellij.plugin.lang.ObjJFile;
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJBinaryDecompiler implements BinaryFileDecompiler {

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile virtualFile) {

        Logger.getLogger(ObjJBinaryDecompiler.class.getCanonicalName()).log(Level.INFO, "running binary decompile from instance of ObjJBinaryDecompiler");
        try {
            return decompileStatic(virtualFile);
        } catch (IOException e) {
            return new StringBuilder("/* !! Decompiler Failed in file: ").append(virtualFile.getName()).append(" with error \"").append(e.getLocalizedMessage()).append("\"").append(" !! */");
        } catch (RecognitionException e) {
            return new StringBuilder("/* !! Decompiler failed with recognition error: ").append(e.getLocalizedMessage()).append(" !! */");
        } catch (Exception e) {
            return new StringBuilder("/* !! Decompiler failed with error: ").append(e.getLocalizedMessage()).append(" !! */");
        }
    }

    @NotNull
    public static CharSequence decompileStatic(VirtualFile virtualFile) throws IOException, RecognitionException {
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
            indicator.setText("d]ecompiling objective-j framework file: " + virtualFile.getName());
        }
        Logger.getLogger(ObjJBinaryDecompiler.class.getCanonicalName()).log(Level.INFO, "running binary decompileSTATIC of ObjJBinaryDecompiler");
        final long startTime = new Date().getTime();
        final ObjJSjLexer lexer;
        StringBuilder out = new StringBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(virtualFile.contentsToByteArray());
        lexer = new ObjJSjLexer(CharStreams.fromStream(inputStream));
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
        Logger.getLogger(ObjJBinaryDecompiler.class.getCanonicalName()).log(Level.INFO, "Time to parse: " + ((new Date().getTime() - startTime)));
        if (indicator != null) {
            indicator.cancel();
        }
        return out;
    }

    @NotNull
    public static List<ObjJFile> getDecompiledFiles(VirtualFile virtualFile, Project project) throws IOException, RecognitionException {
        final ObjJSjLexer lexer;
        List<ObjJFile> files = new ArrayList<>();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(virtualFile.contentsToByteArray());
        lexer = new ObjJSjLexer(CharStreams.fromStream(inputStream));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ObjJSjParser parser = new ObjJSjParser(tokenStream);
        ObjJSjParser.ScriptContext context = parser.script();
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        ObjJSjParserListener listener = new ObjJSjListener();
        parseTreeWalker.walk(listener, context);
        for (FileTemplate fileTemplate : ((ObjJSjListener) listener).getFileTemplates()) {
            StringBuilder out = new StringBuilder();
            fileTemplate.appendTo(out);
            ObjJFile file = ObjJElementFactory.INSTANCE.createFileFromText(project, fileTemplate.getFileName(), out.toString());
            files.add(file);
        }
        return files;
    }
}
