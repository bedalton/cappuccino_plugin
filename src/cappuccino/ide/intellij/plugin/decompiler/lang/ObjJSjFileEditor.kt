package cappuccino.ide.intellij.plugin.decompiler.lang

import cappuccino.ide.intellij.plugin.decompiler.decompiler.ObjJBinaryDecompiler
import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.utils.text
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl
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

    private val document  = DocumentImpl(virtualFile.text())
    private val editor = EditorFactory.getInstance().createEditor(document, project, ObjJFileType.INSTANCE, true)
    private var isActive:Boolean = false
    // GUI
    private var component : JComponent = editor.component

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null
    override fun getComponent(): JComponent {
        Logger.getAnonymousLogger().log(Level.INFO, "ObjJSjFileEditor get component");
        val content = ObjJBinaryDecompiler.decompileStatic(virtualFile)
        document.setText(content)
        return component
    }

    override fun getCurrentLocation(): FileEditorLocation? = null
    override fun getName(): String = "Objective-J Decompiled Files"
    override fun getPreferredFocusedComponent(): JComponent? = component
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
        Disposer.dispose(this)
    }

    override fun setState(state: FileEditorState) {}

}
