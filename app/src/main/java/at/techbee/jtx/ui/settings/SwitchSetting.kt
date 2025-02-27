/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R

enum class SwitchSetting(
    val key: String,
    val icon: @Composable () -> Unit,
    val title: Int,
    val subtitle: Int? = null,
    val default: Boolean
) {
    SETTING_ENABLE_JOURNALS(
        key = "settings_enable_journals",
        icon = { Icon(Icons.AutoMirrored.Outlined.EventNote, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_modules_enable_journals,
        default = true
    ),
    SETTING_ENABLE_NOTES(
        key = "settings_enable_notes",
        icon = { Icon(Icons.AutoMirrored.Outlined.NoteAdd, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_modules_enable_notes,
        default = true
    ),
    SETTING_ENABLE_TASKS(
        key = "settings_enable_tasks",
        icon = { Icon(Icons.Outlined.AddTask, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_modules_enable_tasks,
        default = true
    ),
    SETTING_AUTO_EXPAND_SUBTASKS(
        key = "settings_auto_expand_subtasks",
        icon = { Icon(Icons.Outlined.TaskAlt, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_default_expand_subtasks,
        default = false
    ),
    SETTING_AUTO_EXPAND_SUBNOTES(
        key = "settings_auto_expand_subnotes",
        icon = { Icon(Icons.AutoMirrored.Outlined.Note, null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_default_expand_subnotes,
        default = false
    ),
    SETTING_AUTO_EXPAND_ATTACHMENTS(
        key = "settings_auto_expand_attachments",
        icon = { Icon(
            Icons.Outlined.Attachment, null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_default_expand_attachments,
        default = false
    ),
    SETTING_AUTO_EXPAND_PARENTS(
        key = "settings_auto_expand_parents",
        icon = { Icon(
            Icons.Outlined.SubdirectoryArrowRight, null, modifier = Modifier.padding(
                16.dp
            ))  },
        title = R.string.settings_default_expand_parents,
        default = false
    ),
    SETTING_SHOW_PROGRESS_FOR_MAINTASKS(
        key = "settings_show_progress_for_maintasks_in_list",
        icon = { Icon(
            painterResource(id = R.drawable.ic_progress_task), null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_show_progress_for_maintasks,
        default = false
    ),
    SETTING_SHOW_PROGRESS_FOR_SUBTASKS(
        key = "settings_show_progress_for_subtasks_in_list",
        icon = { Icon(
            painterResource(id = R.drawable.ic_progress_subtask), null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_show_progress_for_subtasks,
        default = false
    ),
    SETTING_DISABLE_ALARMS_FOR_READONLY(
    key = "settings_disable_alarms_for_readonly",
    icon = { Icon(Icons.Outlined.EditOff, contentDescription = null, modifier = Modifier.padding(16.dp)) },
    title = R.string.settings_disable_alarms_for_readonly,
    default = false
    ),
    SETTING_LINK_PROGRESS_TO_SUBTASKS(
        key = "settings_link_progress_to_subtasks",
        icon = { Icon(Icons.Outlined.DoneAll, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_link_progress_to_subtasks,
        subtitle = R.string.settings_attention_experimental_feature,
        default = false
    ),
    SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC(
    key = "settings_keep_status_progress_completed_in_sync",
    icon = { Icon(Icons.Outlined.PublishedWithChanges, contentDescription = null, modifier = Modifier.padding(16.dp)) },
    title = R.string.settings_keep_status_progress_completed_in_sync,
    default = true
    ),
    SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION(
        key = "settings_journals_set_default_current_location",
        icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_create_with_current_location,
        subtitle = R.string.settings_create_with_current_location_sub,
        default = false
    ),
    SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION(
        key = "settings_notes_set_default_current_location",
        icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_create_with_current_location,
        subtitle = R.string.settings_create_with_current_location_sub,
        default = false
    ),
    SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION(
        key = "settings_tasks_set_default_current_location",
        icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_create_with_current_location,
        subtitle = R.string.settings_create_with_current_location_sub,
        default = false
    ),
    SETTING_STICKY_ALARMS(
        key = "settings_sticky_alarms",
        icon = { Icon(Icons.Outlined.Alarm, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_sticky_alarms,
        subtitle = R.string.settings_sticky_alarms_sub,
        default = false
    ),
    SETTING_FULLSCREEN_ALARMS(
        key = "settings_fullscreen_alarms",
        icon = { Icon(Icons.Outlined.Fullscreen, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_fullscreen_alarms,
        subtitle = R.string.settings_fullscreen_alarms_sub,
        default = false
    ),
    SETTING_SYNC_ON_START(
        key = "settings_sync_on_start",
        icon = { Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_sync_on_start,
        default = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU   // only newer phones have that option enabled by default for performance reasons
    ),
    SETTING_SYNC_ON_PULL_REFRESH(
        key = "settings_sync_on_pull_refresh",
        icon = { Icon(Icons.Outlined.SwipeDown, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_sync_on_pull_refresh,
        default = true
    ),
    SETTING_ACCESSIBILITY_MODE(
        key = "settings_accessibility_mode",
        icon = { Icon(Icons.Outlined.FormatSize, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_accessibility_mode,
        subtitle = R.string.settings_accessibility_mode_sub,
        default = false
    );
    fun saveSetting(newSwitchValue: Boolean, prefs: SharedPreferences) = prefs.edit().putBoolean(key, newSwitchValue).apply()

    fun getSetting(prefs: SharedPreferences) = prefs.getBoolean(key, default)
}
