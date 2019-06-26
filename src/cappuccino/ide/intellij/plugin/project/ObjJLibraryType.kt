package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JComponent
import icons.ObjJIcons
import javax.swing.Icon
import com.intellij.openapi.roots.libraries.LibraryType

/**
 * Libarary type for an objective-j frameworkName
 */
class ObjJLibraryType : LibraryType<DummyLibraryProperties>(LIBRARY) {

    override fun createPropertiesEditor(component: LibraryEditorComponent<DummyLibraryProperties>): LibraryPropertiesEditor? {
        return null
    }

    override fun createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile?, project: Project): NewLibraryConfiguration? {
        return LibraryTypeService.getInstance()
                .createLibraryFromFiles(createLibraryRootsComponentDescriptor(), parentComponent, contextDirectory, this, project);
    }

    override fun createLibraryRootsComponentDescriptor(): LibraryRootsComponentDescriptor {
        // todo create descriptor
        return ObjJLibraryRootComponentDescriptor()
    }

    override fun getCreateActionName(): String? {
        return ObjJBundle.message("objj.sources.library.action-name")
    }

    override fun getIcon(properties: DummyLibraryProperties?): Icon? {
        return ObjJIcons.SDK_ICON
    }

    companion object {
        private val LIBRARY: PersistentLibraryKind<DummyLibraryProperties> = object : PersistentLibraryKind<DummyLibraryProperties>(ObjJBundle.message("objj.sources.library.library-name")) {
            override fun createDefaultProperties(): DummyLibraryProperties {
                return DummyLibraryProperties()
            }
        }

        val instance: ObjJLibraryType by lazy {
            LibraryType.EP_NAME.findExtension(ObjJLibraryType::class.java)!!
        }
    }
}