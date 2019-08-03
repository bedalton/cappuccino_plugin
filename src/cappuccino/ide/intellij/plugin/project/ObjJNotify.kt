/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 hsz Jakub Chrzanowski <jakub@hsz.mobi>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.utils.PLUGIN_VERSION
import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId

/**
 * Wrapper function for showing {@link Notification}.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.7
 */
object ObjJNotify {
    private val NOTIFICATION_GROUP: NotificationGroup = NotificationGroup(
            ObjJBundle.message("objj.notification.group"),
            NotificationDisplayType.BALLOON,
            true,
            ToolWindowId.PROJECT_VIEW
    );

    private val NOTIFICATION_GROUP_UPDATE: NotificationGroup = NotificationGroup(
            ObjJBundle.message("objj.notification.group.update"),
            NotificationDisplayType.STICKY_BALLOON,
            true
    );

    /**
     * Shows {@link Notification} in IGNORE_GROUP_UPDATE group.
     *
     * @param project  current project
     */
    public fun showUpdateIndexWarning(project: Project) {
        show(
                project,
                ObjJBundle.message("objj.notification.update-index-warning.title", PLUGIN_VERSION),
                ObjJBundle.message("objj.notification.update-index-warning.content"),
                NOTIFICATION_GROUP_UPDATE,
                NotificationType.WARNING,
                null
        );
    }

    /**
     * Shows {@link Notification} in IGNORE_GROUP_UPDATE group.
     *
     * @param project  current project
     */
    public fun showUpdate(project: Project) {
        show(
                project,
                ObjJBundle.message("objj.notification.update.title", PLUGIN_VERSION),
                ObjJBundle.message("objj.notification.update.content"),
                NOTIFICATION_GROUP_UPDATE,
                NotificationType.INFORMATION,
                NotificationListener.URL_OPENING_LISTENER
        );
    }

    /**
     * Shows {@link Notification} in ".ignore plugin" group.
     *
     * @param project   current project
     * @param title     notification title
     * @param content   notification text
     * @param type      notification type
     */
    public fun show(project: Project, title: String, content: String,
                    type: NotificationType) {
        show(project, title, content, NOTIFICATION_GROUP, type, null);
    }

    /**
     * Shows {@link Notification} in ".ignore plugin" group.
     *
     * @param project   current project
     * @param title     notification title
     * @param content   notification text
     * @param type      notification type
     * @param listener  optional listener
     */
    public fun show(project: Project, title: String, content: String,
                    type: NotificationType, listener: NotificationListener?) {
        show(project, title, content, NOTIFICATION_GROUP, type, listener);
    }

    /**
     * Shows {@link Notification}.
     *
     * @param project  current project
     * @param title    notification title
     * @param group    notification group
     * @param content  notification text
     * @param type     notification type
     * @param listener optional listener
     */
    public fun show(project: Project, title: String, content: String,
                    group: NotificationGroup, type: NotificationType,
                    listener: NotificationListener?) {
        val notification: Notification = group.createNotification(title, content, type, listener);
        Notifications.Bus.notify(notification, project);
    }
}