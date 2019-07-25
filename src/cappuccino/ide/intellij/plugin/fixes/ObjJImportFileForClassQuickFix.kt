package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import cappuccino.ide.intellij.plugin.utils.doesNotMatch
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import icons.ObjJIcons
import javax.swing.Icon

class ObjJImportFileForClassQuickFix(thisFramework:String, private val className:String, private val withSelector:String? = null, private val includeTests:Boolean) : ObjJImportFileQuickFix(thisFramework) {

    override fun getName(): String {
        return ObjJBundle.message("objective-j.inspections.not-imported.fix.name", "class", className)
    }

    override fun getText(): String {
        return ObjJBundle.message("objective-j.inspections.not-imported.fix.name", "class", className)
    }

    override fun getFileChooserTitle()
            = ObjJBundle.message("objective-j.inspections.not-imported.fix.file-chooser-title", "class", className)

    override fun getFileChooserDescription()
            = ObjJBundle.message("objective-j.inspections.not-imported.fix.file-chooser-description", "class", className)

    override fun getPossibleFiles(project: Project) : List<FrameworkFileNode> {
        var classes= ObjJClassDeclarationsIndex.instance[className, project].filter {
            withSelector == null || it.hasMethod(withSelector)
        }
        var files = classes.mapNotNull {
            it.containingObjJFile
        }
        val needsFilter = files.size > 1 || files.any {
            it.name.startsWith("_")
        }

        if (needsFilter) {
            files = filterDown(project, files)
            classes = classes.filter {
                it.containingFile in files
            }
        }
        val included = mutableListOf<VirtualFile>()

        // Import Framework imports
        val frameworkNames = classes.map {
            it.enclosingFrameworkName
        }.distinct().filterNot { it == EMPTY_FRAMEWORK_NAME }.flatMap{ frameworkName ->
            FilenameIndex.getFilesByName(project, "$frameworkName.j",  GlobalSearchScope.everythingScope(project))
                    .filter {
                        frameworkName == it.enclosingFrameworkName
                    }.distinct()
                    .map { file ->
                        FrameworkFileNode(frameworkName, file.virtualFile, "<$frameworkName/$frameworkName.j>", null)
                    }
        }

        // Format class importing files
        val classesOut = classes.sortedBy {
            when {
                it is ObjJImplementationDeclaration && !it.isCategory -> "0${it.classNameString}"
                it is ObjJProtocolDeclaration -> "1${it.classNameString}"
                else -> "2${it.classNameString}${(it as? ObjJImplementationDeclaration)?.categoryNameString.orEmpty()}"
            }
        }.mapNotNull {
            val node= createNode(it) ?: return@mapNotNull null
            if (node.file in included)
                return@mapNotNull null
            included.add(node.file)
            node
        }
        return frameworkNames + classesOut
    }

    private fun createNode(containingClass: ObjJClassDeclarationElement<*>) : FrameworkFileNode? {
        val file = containingClass.containingObjJFile ?: return null
        val frameworkName = file.frameworkName
        val text = formatClassName(containingClass)
        val icon = getIcon(text)
        return FrameworkFileNode(frameworkName = frameworkName, file = file.virtualFile, text = text, icon = icon)
    }


    private fun formatClassName(containingClass: ObjJClassDeclarationElement<*>) : String {
        val base = when {
            containingClass is ObjJProtocolDeclaration -> "@protocol ${containingClass.classNameString}"
            containingClass is ObjJImplementationDeclaration && containingClass.isCategory
            -> "@implementation ${containingClass.classNameString} ${containingClass.categoryName?.text}"
            else -> "@implementation ${containingClass.classNameString}"
        }
        return "$base in ${containingClass.containingFileName}"
    }

    private fun getIcon(classDescriptorString:String) : Icon {
        return when {
            classDescriptorString.startsWith("@protocol") -> ObjJIcons.PROTOCOL_ICON
            classDescriptorString.startsWith("@implementation") && classDescriptorString.contains("(")
                -> ObjJIcons.CATEGORY_ICON
            else -> ObjJIcons.CLASS_ICON
        }
    }

    private fun filterDown(project:Project, files:List<ObjJFile>) : List<ObjJFile> {

        val testFileNameRegex = ".*?test[s]?".toRegex()
        val filesWithoutUnderscore = files.filterNot {
            it.name.startsWith("_")
        }.filter {
            includeTests || testFileNameRegex.doesNotMatch(it.name.toLowerCase())
        }
        if (filesWithoutUnderscore.isNotEmpty())
            return filesWithoutUnderscore

        val importedInFiles = importedInFiles(project, files).filter {
            includeTests || testFileNameRegex.doesNotMatch(it.name.toLowerCase())
        }

        // Only Import files that do not have class declarations
        // Meaning they are purely imports like Foundations.j or Appkit.j
        val purelyImports = importedInFiles.filter {
            it.classDeclarations.isEmpty()
        }

        if (purelyImports.isEmpty())
            return files

        val numFiles = files.size
        var out = purelyImports.filter {
            val imported = it.getImportedFiles(recursive = true, cache = true)
            imported.intersect(files).size == numFiles
        }
        if (out.isNotEmpty()) {
            out = purelyImports
        }
        return out + files
    }

}