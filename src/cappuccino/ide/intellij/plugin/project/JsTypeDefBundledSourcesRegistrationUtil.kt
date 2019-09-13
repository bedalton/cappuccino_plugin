package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.contents
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryTable.ModifiableModel
import com.intellij.psi.search.FilenameIndex
import java.util.logging.Logger


object JsTypeDefBundledSourcesRegistrationUtil {

    private val LOGGER = Logger.getLogger("#"+ JsTypeDefBundledSourcesRegistrationUtil::class.java)
    private const val ROOT_FOLDER = "jstypedef"
    private const val LIBRARY_NAME = "JsTypeDef-Std-Lib"
    private const val VERSION_TEXT_FILE_NAME = "version.txt"
    private var didInit = false

    fun register(module:Module, project:Project) {
        if (didInit) {
            return
        }
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).smartInvokeLater {
                register(module, project)
            }
            return
        }
        didInit = true
        val moduleScope = module.moduleContentWithDependenciesScope
        if (FilenameIndex.getAllFilesByExt(module.project, "j", moduleScope).isEmpty()) {
            LOGGER.info("There are no .j files in project")
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

        val newVersion = libraryPath.findFileByRelativePath("version.txt")?.contents
                ?: throw Exception("JsTypeDef library versions cannot be null")

        // Check if same version
        if (newVersion == ObjJPluginSettings.typedefVersion) {
            LOGGER.info("JsTypeDef version is the same <$newVersion> in ")
            //return true
        }

        val library = cleanAndReturnLibrary(modifiableModel = modifiableModel)
                ?: modifiableModel.createLibrary(LIBRARY_NAME, ObjJLibraryType.LIBRARY)
        val libModel = library.modifiableModel
        libModel.addRoot(libraryPath, OrderRootType.SOURCES)
        libModel.commit()
        modifiableModel.commit()
        rootModel.commit()
        ObjJPluginSettings.typedefVersion = newVersion
        LOGGER.info("Updated JsTypeDef version to $newVersion")
        return true
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