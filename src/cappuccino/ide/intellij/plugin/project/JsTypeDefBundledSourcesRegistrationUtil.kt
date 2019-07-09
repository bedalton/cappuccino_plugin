package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNamespaceIndex
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.contents
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryTable.ModifiableModel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import java.util.logging.Logger


object JsTypeDefBundledSourcesRegistrationUtil {

    private val LOGGER = Logger.getLogger("#"+ JsTypeDefBundledSourcesRegistrationUtil::class.java)
    private const val ROOT_FOLDER = "jstypedef"
    private const val LIBRARY_NAME = "JsTypeDef-Std-Lib"
    private const val VERSION_TEXT_FILE_NAME = "version.txt"

    fun register(module:Module, project:Project) {

        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).smartInvokeLater {
                register(module, project)
            }
            return
        }
        if (JsTypeDefClassesByNameIndex.instance.containsKey("Document", project)
                && JsTypeDefClassesByNameIndex.instance.containsKey("Window", project)
                && JsTypeDefPropertiesByNamespaceIndex.instance.containsKey("Window.document", project)
        ) {
            LOGGER.info("Project already has typedef definitions")
            //return
        }
        val moduleScope = module.moduleContentWithDependenciesScope
        if (!FilenameIndex.getAllFilesByExt(module.project, ".j", moduleScope).isEmpty()) {
            return
        }
        runWriteAction {
            registerSourcesAsLibrary(module)
        }
    }

    fun deregisterSources(module:Module) {
        val rootModel = ModuleRootManager.getInstance(module).modifiableModel
        val modifiableModel = rootModel.moduleLibraryTable.modifiableModel
        val oldLibrary = modifiableModel.getLibraryByName(LIBRARY_NAME) ?: return
        oldLibrary.modifiableModel.removeRoot(ROOT_FOLDER, OrderRootType.SOURCES)
    }


    private fun registerSourcesAsLibrary(module:Module) : Boolean {
        val project = module.project
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).smartInvokeLater {
                registerSourcesAsLibrary(module)
            }
            return false
        }
        val rootModel = ModuleRootManager.getInstance(module).modifiableModel
        val modifiableModel = rootModel.moduleLibraryTable.modifiableModel
        val libraryPath = ObjJFileUtil.getPluginResourceFile("$BUNDLE_DEFINITIONS_FOLDER/$ROOT_FOLDER")
        if (libraryPath == null) {
            val pluginRoot = ObjJFileUtil.PLUGIN_HOME_DIRECTORY
            if (pluginRoot == null || !pluginRoot.exists()) {
                LOGGER.severe("Failed to locate bundled jstypedef files: Plugin root is invalid")
            } else {
                LOGGER.severe("Failed to locate bundled jstypedef files: Files in plugin root is <${pluginRoot.children?.map { it.name }}>")
            }
            return false
        }

        // Check if same version
        if (isSourceCurrent(libraryPath, modifiableModel)) {
            LOGGER.info("Source jstypedef files are current")
            //return true
        }

        val library = cleanAndReturnLibrary(modifiableModel = modifiableModel)
                ?: modifiableModel.createLibrary(LIBRARY_NAME, ObjJLibraryType.LIBRARY)
        val libModel = library.modifiableModel
        libModel.addRoot(libraryPath, OrderRootType.SOURCES)
        libModel.commit()
        modifiableModel.commit()
        rootModel.commit()
        return true
    }

    private fun isSourceCurrent(newLibraryPath: VirtualFile?, model:ModifiableModel) : Boolean {
        val versionString = newLibraryPath?.findFileByRelativePath("version.txt")?.contents
        val oldVersionString = currentLibraryVersion(model)
        if (versionString == null && oldVersionString == null)
            throw Exception("JsTypeDef library versions cannot be null")
        return versionString == oldVersionString
    }


    private fun cleanAndReturnLibrary(modifiableModel: ModifiableModel) : Library? {
        val oldLibrary = modifiableModel.getLibraryByName(LIBRARY_NAME) ?: return null
        oldLibrary.modifiableModel.removeRoot(ROOT_FOLDER, OrderRootType.SOURCES)
        return oldLibrary
    }


    private fun currentLibraryVersion(model:ModifiableModel) : String? {
        return model.getLibraryByName(LIBRARY_NAME)
                ?.getFiles(OrderRootType.SOURCES)
                .orEmpty().firstOrNull { it.name == VERSION_TEXT_FILE_NAME }
                ?.contents
    }


}