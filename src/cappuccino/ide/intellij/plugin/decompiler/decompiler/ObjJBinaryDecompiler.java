package cappuccino.ide.intellij.plugin.decompiler.decompiler;

import cappuccino.decompiler.parser.*;
import cappuccino.decompiler.parser.ObjJSjListener.ProgressCallback;
import cappuccino.decompiler.templates.manual.FileTemplate;
import cappuccino.ide.intellij.plugin.lang.ObjJFile;
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javassist.bytecode.ByteArray;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            return parse(virtualFile);
        } catch (IOException e) {
            return new StringBuilder("/* !! Decompiler Failed in file: ").append(virtualFile.getName()).append(" with error \"").append(e.getLocalizedMessage()).append("\"").append(" !! */");
        } catch (RecognitionException e) {
            return new StringBuilder("/* !! Decompiler failed with recognition error: ").append(e.getLocalizedMessage()).append(" !! */");
        } catch (Exception e) {
            return new StringBuilder("/* !! Decompiler failed with error: ").append(e.getLocalizedMessage()).append(" !! */");
        }
    }

    @NotNull
    public static void decompileStatic(@NotNull Project project, @NotNull final VirtualFile virtualFile, @NotNull final DecompilerCallback decompilerCallback) throws IOException, RecognitionException {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Title"){
            public void run(@NotNull final ProgressIndicator indicator) {
                StringBuilder out;
                indicator.setText("Decompiling objective-j framework file: " + virtualFile.getName());
                indicator.setIndeterminate(true);
                try {
                    Logger.getLogger(ObjJBinaryDecompiler.class.getCanonicalName()).log(Level.INFO, "running binary decompileSTATIC of ObjJBinaryDecompiler");
                    out = parse(virtualFile, indicator::setFraction);
                } catch (IOException e) {
                    out = new StringBuilder("/* !! Decompiler Failed in file: ").append(virtualFile.getName()).append(" with error \"").append(e.getLocalizedMessage()).append("\"").append(" !! */");
                    indicator.cancel();
                } catch (RecognitionException e) {
                    out = new StringBuilder("/* !! Decompiler failed with recognition error: ").append(e.getLocalizedMessage()).append(" !! */");
                    indicator.cancel();
                } catch (Exception e) {
                    out = new StringBuilder("/* !! Decompiler failed with error: ").append(e.getLocalizedMessage()).append(" !! */");
                    indicator.cancel();
                }
                if (out == null) {
                    out = new StringBuilder("/* Decompiler failed without exception */");
                }
                // Finished
                indicator.setFraction(1.0);
                indicator.setText("finished decompiling objective-j file");
                decompilerCallback.onDecompile(out);
            }});
    }

    public static StringBuilder parse(@NotNull final VirtualFile virtualFile) throws IOException, RecognitionException {
        return parse(virtualFile, null);
    }

    public static StringBuilder parse(@NotNull final VirtualFile virtualFile, @Nullable final ProgressCallback progressCallback) throws IOException, RecognitionException {
        StringBuilder out = new StringBuilder();
        final long startTime = new Date().getTime();
        byte[] byteArray = virtualFile.contentsToByteArray();
        Logger.getAnonymousLogger().log(Level.INFO, "NumBytes in Virtual File: "+byteArray.length);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        if (progressCallback != null) {
            progressCallback.onProgress(0);
        } else {
            Logger.getAnonymousLogger().log(Level.INFO, "No Progress callback provided");
        }
        final ObjJSjLexer lexer;
        lexer = new ObjJSjLexer(CharStreams.fromStream(inputStream));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ObjJSjParser parser = new ObjJSjParser(tokenStream);
        parser.setProgressCallback(progressCallback);
        ObjJSjParser.ScriptContext context = parser.script();
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        ObjJSjParserListener listener = new ObjJSjListener(byteArray.length, progressCallback);
        parseTreeWalker.walk(listener, context);
        for (FileTemplate fileTemplate : ((ObjJSjListener) listener).getFileTemplates()) {
            fileTemplate.appendTo(out);
            out.append("\n");
        }
        Logger.getLogger(ObjJBinaryDecompiler.class.getCanonicalName()).log(Level.INFO, "Time to parse: " + ((new Date().getTime() - startTime)));
        return out;
    }
    @NotNull
    public static List<ObjJFile> getDecompiledFiles(VirtualFile virtualFile, Project project) throws IOException, RecognitionException {
        return getDecompiledFiles(virtualFile, project, null);
    }
    @NotNull
    public static List<ObjJFile> getDecompiledFiles(VirtualFile virtualFile, Project project, ProgressCallback progressCallback) throws IOException, RecognitionException {
        final ObjJSjLexer lexer;
        List<ObjJFile> files = new ArrayList<>();
        byte[] byteArray = virtualFile.contentsToByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        lexer = new ObjJSjLexer(CharStreams.fromStream(inputStream));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ObjJSjParser parser = new ObjJSjParser(tokenStream);
        ObjJSjParser.ScriptContext context = parser.script();
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        ObjJSjParserListener listener = new ObjJSjListener(byteArray.length, progressCallback);
        parseTreeWalker.walk(listener, context);
        for (FileTemplate fileTemplate : ((ObjJSjListener) listener).getFileTemplates()) {
            StringBuilder out = new StringBuilder();
            fileTemplate.appendTo(out);
            ObjJFile file = ObjJElementFactory.INSTANCE.createFileFromText(project, fileTemplate.getFileName(), out.toString());
            files.add(file);
        }
        return files;
    }

    public static interface DecompilerCallback {
        void onDecompile(StringBuilder stringBuilder);
    }
}