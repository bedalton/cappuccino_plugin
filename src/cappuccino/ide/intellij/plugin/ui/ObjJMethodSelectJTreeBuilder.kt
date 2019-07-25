package cappuccino.ide.intellij.plugin.ui

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.containingFileName
import cappuccino.ide.intellij.plugin.psi.utils.isOrHasParentOfType
import com.intellij.ui.treeStructure.Tree
import icons.ObjJIcons
import java.awt.Component
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

object ObjJMethodSelectJTreeBuilder {
    internal fun build(files: List<ObjJMethodHeader>, onSelect: (List<ObjJMethodHeader>) -> Unit): JTree {
        val treeRoot = getRootNodeForFiles(files)
        val tree = Tree(treeRoot)
        tree.isRootVisible = false
        tree.addTreeSelectionListener {event ->
            val selected = tree.selectionPaths.mapNotNull {
                (it.lastPathComponent as? MethodNode)?.method
            }
            onSelect(selected)
        }
        tree.cellRenderer = CustomTreeCellRenderer
        return tree
    }

    private fun getRootNodeForFiles(files: List<ObjJMethodHeader>) : MutableTreeNode {
        val methodsMap: Map<String, List<ObjJMethodHeader>> = mapMethodsByClass(files)
        val treeRoot = DefaultMutableTreeNode("Root")
        for ((className, methods) in methodsMap) {
            if (methods.isEmpty())
                continue
            val classNode = ClassTreeNode(className, getIcon(className))
            val methodNodes = methods.map {
                val isOptional = it.getParentOfType(ObjJProtocolScopedMethodBlock::class.java)?.atOptional != null
                MethodNode(className, it.text, it, isOptional)
            }
            appendMethods(classNode, methodNodes)
            treeRoot.add(classNode)
        }
        return treeRoot
    }

    private fun appendMethods(frameworkNode:ClassTreeNode, methodNodes:List<MethodNode>) {
        val hasOptionals = methodNodes.any {it.isOptional}
        if (hasOptionals) {
            appendWithOptionals(frameworkNode, methodNodes)
        } else {
            for (methodNode in methodNodes) {
                frameworkNode.add(methodNode)
            }
        }
    }

    private fun appendWithOptionals(frameworkNode:ClassTreeNode, methodNodes:List<MethodNode>) {
        val required = DefaultMutableTreeNode("@required")
        frameworkNode.add(required)
        val optional = DefaultMutableTreeNode("@optional")
        frameworkNode.add(optional)
        for (methodNode in methodNodes) {
            if (methodNode.isOptional) {
                optional.add(methodNode)
            } else {
                required.add(methodNode)
            }
        }
    }


    private fun mapMethodsByClass(methods: List<ObjJMethodHeader>): Map<String, List<ObjJMethodHeader>> {
        val methodMap = mutableMapOf<String, MutableList<ObjJMethodHeader>>()
        for (method in methods) {
            val containingClass = method.containingClass
            val className = if (containingClass != null && !method.isOrHasParentOfType(ObjJSelectorLiteral::class.java)) formatClassName(containingClass) else "@selector"
            val list = methodMap[className] ?: mutableListOf()
            list.add(method)
            methodMap[className] = list
        }
        return methodMap
    }

    private data class ClassTreeNode(internal val className: String, val icon:Icon) : DefaultMutableTreeNode(this)

    internal data class MethodNode(internal val className: String, internal val selector:String, internal val method: ObjJMethodHeader, internal val isOptional:Boolean) : DefaultMutableTreeNode(this) {
        val text: String
            get() = "${method.text} in $className"
    }

    private fun formatClassName(containingClass:ObjJClassDeclarationElement<*>) : String {
        val base = when {
            containingClass is ObjJProtocolDeclaration -> "@protocol ${containingClass.classNameString}"
            containingClass is ObjJImplementationDeclaration && containingClass.isCategory
                -> "@implementation ${containingClass.classNameString} (${containingClass.categoryNameString})"
            else -> "@implementation ${containingClass.classNameString}"
        }
        return "$base in ${containingClass.containingFileName}"
    }

    private fun getIcon(classDescriptorString:String) : Icon {
        return when {
            classDescriptorString.startsWith("@protocol") -> ObjJIcons.PROTOCOL_ICON
            classDescriptorString.startsWith("@implementation") && classDescriptorString.contains("(")
                -> ObjJIcons.CATEGORY_ICON
            classDescriptorString.startsWith("@implementation") -> ObjJIcons.CLASS_ICON
            else -> ObjJIcons.SELECTOR_ICON
        }
    }

    internal object CustomTreeCellRenderer : javax.swing.tree.DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(tree: JTree, value: Any, selected: Boolean, expanded: Boolean,
                                                  leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel
            when (value) {
                is ClassTreeNode -> label.text = value.className
                is MethodNode -> label.text = value.text
                is DefaultMutableTreeNode -> if (value.userObject is String) value.userObject else value.userObject.toString()
                else -> label.text = value.toString()
            }

            return label
        }
    }
}