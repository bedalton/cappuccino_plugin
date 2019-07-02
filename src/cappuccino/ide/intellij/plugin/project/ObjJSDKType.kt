package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.utils.*
import com.intellij.openapi.projectRoots.*
import icons.ObjJIcons
import org.jdom.Element

import java.io.*
import java.nio.charset.Charset
import java.util.logging.Logger
import javax.swing.Icon
import com.intellij.openapi.projectRoots.SdkModificator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.OrderRootType
import com.intellij.util.PathUtil
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.JavadocOrderRootType
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiElement


class ObjJSDKType : SdkType(SDK_TYPE_ID) {

    override fun suggestHomePath(): String? {
        val path = PropertiesComponent.getInstance().getValue(LAST_SELECTED_SDK_HOME_KEY)
        if (path != null) return PathUtil.getParentPath(path)

        return null
    }

    override fun isValidSdkHome(sdkPath: String): Boolean {
        return findFrameworkFolders(sdkPath).isNotEmpty()
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String?): String {
        return ObjJBundle.message("objj.sources.sdk.suggest-name", getVersionString(sdkHome) ?: "").trim()
    }

    override fun getIcon(): Icon {
        return ObjJIcons.SDK_ICON
    }

    override fun getIconForAddAction(): Icon {
        return ObjJIcons.SDK_ADD_ICON
    }

    override fun getVersionString(sdkHome: String?) : String? {
        if (sdkHome == null)
            return ""
        val versionFile = File(sdkHome, "version.json")
        if (!versionFile.exists())
            return null
        val text = versionFile.readText(Charset.defaultCharset())
        val matcher = SDK_VERSION_REGEX.find(text) ?: return null
        return matcher.groupValues.getOrNull(1)
    }

    override fun createAdditionalDataConfigurable(
            sdkModel: SdkModel,
            sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun getName(): String {
        return SDK_NAME
    }

    override fun getPresentableName(): String {
        return SDK_NAME
    }

    override fun saveAdditionalData(
            sdkAdditionalData: SdkAdditionalData,
            element: Element) {
    }

    override fun setupSdkPaths(sdk: Sdk) {
        val modificator = sdk.sdkModificator
        setupSdkPaths(sdk.homeDirectory, modificator)
        modificator.commitChanges()
        LOGGER.info("Setup paths")
    }

    private fun setupSdkPaths(sdkRoot: VirtualFile?, sdkModificator: SdkModificator) {
        if (sdkRoot == null || !sdkRoot.isValid) {
            return
        }
        PropertiesComponent.getInstance().setValue(LAST_SELECTED_SDK_HOME_KEY, sdkRoot.path)
        sdkRoot.refresh(false, true)
        sdkModificator.versionString = getVersionString(sdkRoot.path)
        val frameworkDirectories = findFrameworkFolders(sdkRoot.path)
        for ((frameworkName, directory) in frameworkDirectories) {
            LOGGER.info("Adding Framework: $frameworkName")
            findAndAddSourceRoots(directory, sdkModificator)
        }
    }

    private fun findFrameworkFolders(homePath:String) : List<Pair<String, VirtualFile>> {
        val file = ObjJVirtualFileUtil.findFileByPath(homePath) ?: return emptyList()
        if (!file.exists() || !file.isDirectory) {
            return emptyList()
        }

        return file.children.filter { it.exists() && it.isDirectory }.mapNotNull {directory ->
            val plist = directory.children.firstOrNull { it.exists() && !it.isDirectory && it.name.toLowerCase() == INFO_PLIST_FILE_NAME_TO_LOWER_CASE } ?: return@mapNotNull null
            val frameworkName = findFrameworkNameInPlistText(plist.contents) ?: return@mapNotNull null
            LOGGER.info("Found Framework: $frameworkName")
            Pair(frameworkName, directory)
        }
    }
    private fun findAndAddSourceRoots(dir: VirtualFile, sdkModificator: SdkModificator) {
        sdkModificator.addRoot(dir, OrderRootType.SOURCES)
        /*
        val visitor = object : VirtualFileVisitor<Any>(SKIP_ROOT) {
            override fun visitFile(child: VirtualFile): Boolean {
                if (child.isDirectory) {
                    sdkModificator.addRoot(child, OrderRootType.SOURCES)
                    return false
                }
                return true
            }
        }
        VfsUtilCore.visitChildrenRecursively(dir, visitor)
         */
    }
    override fun isRootTypeApplicable(type: OrderRootType): Boolean {
        return type === OrderRootType.CLASSES || type === OrderRootType.SOURCES || type === JavadocOrderRootType.getInstance()
    }


    companion object {
        private const val SDK_TYPE_ID = "ObjJ.Framework"
        private val SDK_NAME:String by lazy {
            ObjJBundle.message("objj.sources.sdk.name")
        }
        private val SDK_VERSION_REGEX = "\"version\"\\s*:\\s*\"([^\"]+?)\"".toRegex()
        private val LOGGER = Logger.getLogger("#${this::class.java.simpleName}")
        private const val LAST_SELECTED_SDK_HOME_KEY = "objj.sdk.LAST_HOME_PATH"
        val instance by lazy {
            findInstance(ObjJSDKType::class.java)
        }
        fun getSDK(element:PsiElement) : Sdk? {
            val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return null
            return ModuleRootManager.getInstance(module).sdk
        }
    }
}
