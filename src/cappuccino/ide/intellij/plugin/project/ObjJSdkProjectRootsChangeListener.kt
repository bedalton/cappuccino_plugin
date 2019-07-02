package cappuccino.ide.intellij.plugin.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import java.util.logging.Logger

object ObjJSdkProjectRootsChangeListener : ModuleRootListener {

    val LOGGER = Logger.getLogger("#"+ObjJSdkProjectRootsChangeListener::class.java)

    override fun beforeRootsChange(event: ModuleRootEvent) {
        super.beforeRootsChange(event)

    }

    override fun rootsChanged(event: ModuleRootEvent) {
        super.rootsChanged(event)
        val project = event.source as? Project
        if (project == null) {
            LOGGER.severe("SdkRootsChangeListener: Failed to get project on roots change. Object is of type: ${event.source.javaClass.canonicalName}")
            return
        }
        project

    }

}