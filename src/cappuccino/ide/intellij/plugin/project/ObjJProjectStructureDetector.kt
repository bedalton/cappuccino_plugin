package cappuccino.ide.intellij.plugin.project


import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFileType
import com.intellij.ide.util.importProject.ModuleDescriptor
import com.intellij.ide.util.importProject.ProjectDescriptor
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector
import java.io.File

class ObjJProjectStructureDetector : ProjectStructureDetector() {
    override fun detectRoots(
            dir: File,
            children: Array<out File>,
            base: File,
            result: MutableList<DetectedProjectRoot>
    ): DirectoryProcessingResult {
        if (true || children.any { it.name.endsWith(".j") || it.name.endsWith(JsTypeDefFileType.FILE_EXTENSION) }) {
            result.add(object : DetectedProjectRoot(dir) {
                override fun getRootTypeName(): String = "Objective-J"
            })
        }

        return DirectoryProcessingResult.SKIP_CHILDREN
    }

    override fun setupProjectStructure(
            roots: MutableCollection<DetectedProjectRoot>,
            projectDescriptor: ProjectDescriptor,
            builder: ProjectFromSourcesBuilder
    ) {
        val root = roots.singleOrNull()
        if (root == null || builder.hasRootsFromOtherDetectors(this) || projectDescriptor.modules.isNotEmpty()) {
            return
        }
        val moduleDescriptor = ModuleDescriptor(root.directory, ObjJModuleType.instance, emptyList<DetectedSourceRoot>())
        projectDescriptor.modules = listOf(moduleDescriptor)
    }
}