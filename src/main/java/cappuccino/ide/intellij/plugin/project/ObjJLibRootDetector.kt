/*
 * Copyright 2000-2013 JetBrains s.r.o.
 * Copyright 2014-2014 AS3Boyan
 * Copyright 2014-2014 Elias Ku
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * modified by Daniel Badal - 2019
 */

package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.utils.INFO_PLIST_FILE_NAME
import cappuccino.ide.intellij.plugin.utils.contents
import cappuccino.ide.intellij.plugin.utils.findFrameworkNameInPlistText
import cappuccino.ide.intellij.plugin.utils.notEquals
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.ui.RootDetector
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.util.*

/**
 * @author: Fedor.Korotkov
 */
class ObjJLibRootDetector internal constructor(rootType: OrderRootType, presentableRootTypeName: String) : RootDetector(rootType, false, presentableRootTypeName) {

    override fun detectRoots(rootCandidate: VirtualFile, progressIndicator: ProgressIndicator): Collection<VirtualFile> {
        val result = ArrayList<VirtualFile>()
        collectRoots(rootCandidate, result, progressIndicator)
        return result
    }

    companion object {

        fun collectRoots(file: VirtualFile, result: MutableList<VirtualFile>, progressIndicator: ProgressIndicator?) {
            if (file.fileSystem is JarFileSystem) {
                return
            }
            VfsUtilCore.visitChildrenRecursively(file, object : VirtualFileVisitor<Any>() {
                override fun visitFile(thisFile: VirtualFile): Boolean {
                    progressIndicator?.checkCanceled()
                    if (!thisFile.isDirectory)
                        return false
                    if (containsFrameworkFiles(thisFile)) {
                        result.add(thisFile)
                    }
                    return true
                }
            })
        }

        private fun containsFrameworkFiles(dir: VirtualFile): Boolean {
            val result = VfsUtilCore.visitChildrenRecursively(dir, object : VirtualFileVisitor<Any>() {
                override fun visitFileEx(file: VirtualFile): Result {
                    if (file.isDirectory)
                        return CONTINUE
                    if (INFO_PLIST_FILE_NAME.notEquals(file.name, true))
                        return CONTINUE
                    return if (findFrameworkNameInPlistText(file.contents) != null)  skipTo(dir) else CONTINUE
                }
            })
            return result.skipToParent != null
        }
    }
}