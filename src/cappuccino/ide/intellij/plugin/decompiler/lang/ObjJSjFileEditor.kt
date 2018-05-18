package cappuccino.ide.intellij.plugin.decompiler.lang

import cappuccino.ide.intellij.plugin.decompiler.decompiler.ObjJBinaryDecompiler
import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.utils.text
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.editor.impl.EditorFactoryImpl
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import java.awt.Point
import java.beans.PropertyChangeListener
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.text.EditorKit

class ObjJSjFileEditor (
    private val virtualFile: VirtualFile,
    private val project: Project
) : UserDataHolderBase(), FileEditor {

    private val document  = DocumentImpl("/* Decompiling... */")
    private val editor = EditorFactory.getInstance().createEditor(document, project, ObjJFileType.INSTANCE, true)
    private var isActive:Boolean = false
    private var decompiling:Boolean = false
    private var didSet:Boolean = false;
    // GUI

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null
    override fun getComponent(): JComponent {
        if (didSet || decompiling) {
            return editor.component
        } else if (!didSet) {
            didSet = true
            Logger.getAnonymousLogger().log(Level.INFO, "ObjJSjFileEditor get component")

            ObjJBinaryDecompiler.decompileStatic(project, virtualFile, { content ->
                ApplicationManager.getApplication().invokeLater({
                    ApplicationManager.getApplication().runWriteAction({
                        document.setText(content)
                    })
                })
            })
        }
        return editor.component
    }

    override fun getCurrentLocation(): FileEditorLocation? = null
    override fun getName(): String = "Objective-J Decompiled Files"
    override fun getPreferredFocusedComponent(): JComponent? = editor.component
    override fun isModified(): Boolean = false
    override fun isValid() = true

    override fun selectNotify() {
        isActive = true
//
//        if (isObsolete) {
//            update()
//        }
    }

    override fun deselectNotify() {
        isActive = true
    }

    override fun dispose() {
        EditorFactoryImpl.getInstance().releaseEditor(editor)
        Disposer.dispose(this)
    }

    override fun setState(state: FileEditorState) {}

}
