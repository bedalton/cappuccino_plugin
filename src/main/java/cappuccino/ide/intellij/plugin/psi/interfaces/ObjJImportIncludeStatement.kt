package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.lang.ObjJFile

interface ObjJImportIncludeStatement : ObjJCompositeElement {
    val frameworkNameString:String?
    val fileNameString:String?
    val importIncludeElement:ObjJImportElement<*>?
    fun resolve():ObjJFile?
    fun multiResolve():List<ObjJFile>
}