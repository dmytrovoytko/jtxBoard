/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.preference.PreferenceManager

class SettingsStateHolder(val context: Context) {

    companion object {
        private const val SETTINGS_PRO_INFO_SHOWN = "settingsProInfoShown"
        private const val PREFS_LAST_COLLECTION = "lastUsedCollection"
        private const val PREFS_SAVE_AND_EDIT = "saveAndEdit"
    }

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var settingTheme = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_THEME.key, DropdownSetting.SETTING_THEME.default.key) } ?: DropdownSetting.SETTING_THEME.default )
    var settingAudioFormat = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_AUDIO_FORMAT.key, DropdownSetting.SETTING_AUDIO_FORMAT.default.key) } ?: DropdownSetting.SETTING_AUDIO_FORMAT.default )
    var settingAutoExpandSubtasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.key, SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.default))
    var settingAutoExpandSubnotes = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.key, SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.default))
    var settingAutoExpandAttachments = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.key, SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.default))

    var settingShowProgressForMainTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST.key, SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST.default))
    var settingShowProgressForSubTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.key, SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.default))
    var settingShowSubtasksInTasklist = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_SUBTASKS_IN_TASKLIST.key, SwitchSetting.SETTING_SHOW_SUBTASKS_IN_TASKLIST.default))
    var settingShowSubnotesInNoteslist = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_SUBNOTES_IN_NOTESLIST.key, SwitchSetting.SETTING_SHOW_SUBNOTES_IN_NOTESLIST.default))
    var settingShowSubjournalsInJournallist = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST.key, SwitchSetting.SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST.default))

    var settingAutosave = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_DETAILS_AUTOSAVE.key, SwitchSetting.SETTING_DETAILS_AUTOSAVE.default))

    var settingDefaultStartDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_DEFAULT_START_DATE.key, DropdownSetting.SETTING_DEFAULT_START_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_START_DATE.default )
    var settingDefaultDueDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_DEFAULT_DUE_DATE.key, DropdownSetting.SETTING_DEFAULT_DUE_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_DUE_DATE.default )
    var settingStepForProgress = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_PROGRESS_STEP.key, DropdownSetting.SETTING_PROGRESS_STEP.default.key) } ?: DropdownSetting.SETTING_PROGRESS_STEP.default )
    var settingDisableAlarmsReadonly = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_DISABLE_ALARMS_FOR_READONLY.key, SwitchSetting.SETTING_DISABLE_ALARMS_FOR_READONLY.default))


    //invisible settings
    var proInfoShown = mutableStateOf(prefs.getBoolean(SETTINGS_PRO_INFO_SHOWN, false))
        set(newValue) {
            prefs.edit().putBoolean(SETTINGS_PRO_INFO_SHOWN, newValue.value).apply()
            field = newValue
        }

    var lastUsedCollection = mutableStateOf(prefs.getLong(PREFS_LAST_COLLECTION, 0L))
        set(newValue) {
            prefs.edit().putLong(PREFS_LAST_COLLECTION, newValue.value).apply()
            field = newValue
        }

    var saveAndEdit = mutableStateOf(prefs.getBoolean(PREFS_SAVE_AND_EDIT, false))
        set(newValue) {
            prefs.edit().putBoolean(PREFS_SAVE_AND_EDIT, newValue.value).apply()
            field = newValue
        }
}


