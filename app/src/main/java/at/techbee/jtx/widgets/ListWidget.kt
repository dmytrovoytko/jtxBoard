/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.ListWidgetConfigActivity
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.CheckboxPosition
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.UiUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

const val MAX_WIDGET_ENTRIES = 50

class ListWidget : GlanceAppWidget() {

    companion object {
        val filterConfig = stringPreferencesKey("filter_config")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            //Log.d("ListWidget", "appWidgetId in ListWidget: ${GlanceAppWidgetManager(context).getAppWidgetId(LocalGlanceId.current)}")
            //Log.d("ListWidget", "glanceId in ListWidget: ${LocalGlanceId.current}")
            val database = ICalDatabase.getInstance(context).iCalDatabaseDao()

            val state = currentState<Preferences>()
            val listWidgetConfig = remember(state) {
                state[filterConfig]?.let {
                    Json.decodeFromString<ListWidgetConfig>(it)
                } ?: ListWidgetConfig()
            }

            // legacy handling
            if(listWidgetConfig.checkboxPositionEnd)
                listWidgetConfig.checkboxPosition = CheckboxPosition.END

            val listQuery = remember(listWidgetConfig) {
                ICal4List.constructQuery(
                    modules = listOf(listWidgetConfig.module),
                    searchCategories = listWidgetConfig.searchCategories,
                    searchResources = listWidgetConfig.searchResources,
                    searchStatus = listWidgetConfig.searchStatus,
                    searchClassification = listWidgetConfig.searchClassification,
                    searchPriority = listWidgetConfig.searchPriority,
                    searchCollection = listWidgetConfig.searchCollection,
                    searchAccount = listWidgetConfig.searchAccount,
                    orderBy = listWidgetConfig.orderBy,
                    sortOrder = listWidgetConfig.sortOrder,
                    orderBy2 = listWidgetConfig.orderBy2,
                    sortOrder2 = listWidgetConfig.sortOrder2,
                    isExcludeDone = listWidgetConfig.isExcludeDone,
                    isFilterOverdue = listWidgetConfig.isFilterOverdue,
                    isFilterDueToday = listWidgetConfig.isFilterDueToday,
                    isFilterDueTomorrow = listWidgetConfig.isFilterDueTomorrow,
                    isFilterDueWithin7Days = listWidgetConfig.isFilterDueWithin7Days,
                    isFilterDueFuture = listWidgetConfig.isFilterDueFuture,
                    isFilterStartInPast = listWidgetConfig.isFilterStartInPast,
                    isFilterStartToday = listWidgetConfig.isFilterStartToday,
                    isFilterStartTomorrow = listWidgetConfig.isFilterStartTomorrow,
                    isFilterStartWithin7Days = listWidgetConfig.isFilterStartWithin7Days,
                    isFilterStartFuture = listWidgetConfig.isFilterStartFuture,
                    isFilterNoDatesSet = listWidgetConfig.isFilterNoDatesSet,
                    isFilterNoStartDateSet = listWidgetConfig.isFilterNoStartDateSet,
                    isFilterNoDueDateSet = listWidgetConfig.isFilterNoDueDateSet,
                    isFilterNoCompletedDateSet = listWidgetConfig.isFilterNoCompletedDateSet,
                    filterStartRangeStart = listWidgetConfig.filterStartRangeStart,
                    filterStartRangeEnd = listWidgetConfig.filterStartRangeEnd,
                    filterDueRangeStart = listWidgetConfig.filterDueRangeStart,
                    filterDueRangeEnd = listWidgetConfig.filterDueRangeEnd,
                    filterCompletedRangeStart = listWidgetConfig.filterCompletedRangeStart,
                    filterCompletedRangeEnd = listWidgetConfig.filterCompletedRangeEnd,
                    isFilterNoCategorySet = listWidgetConfig.isFilterNoCategorySet,
                    isFilterNoResourceSet = listWidgetConfig.isFilterNoResourceSet,
                    flatView = listWidgetConfig.flatView,  // always true in Widget, we handle the flat view in the code
                    searchSettingShowOneRecurEntryInFuture = listWidgetConfig.showOneRecurEntryInFuture,
                    hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
                    limit = MAX_WIDGET_ENTRIES
                )
            }
            val list by remember(listQuery) { database.getIcal4ListFlow(listQuery) }.collectAsState(initial = emptyList())

            val subtasksQuery = remember(listWidgetConfig) {
                ICal4List.getQueryForAllSubEntries(
                    component = Component.VTODO,
                    hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
                    orderBy = listWidgetConfig.subtasksOrderBy,
                    sortOrder = listWidgetConfig.subtasksSortOrder,
                    searchText = null
                )
            }
            val subnotesQuery = remember(listWidgetConfig) {
                ICal4List.getQueryForAllSubEntries(
                    component = Component.VJOURNAL,
                    hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
                    orderBy = listWidgetConfig.subnotesOrderBy,
                    sortOrder = listWidgetConfig.subnotesSortOrder,
                    searchText = null
                )
            }
            val subtasks by remember(subtasksQuery) { database.getSubEntriesFlow(subtasksQuery) }.collectAsState(initial = emptyList())
            val subnotes by remember(subnotesQuery) { database.getSubEntriesFlow(subnotesQuery) }.collectAsState(initial = emptyList())

            val scope = rememberCoroutineScope()

            GlanceTheme {

                ListWidgetContent(
                    listWidgetConfig,
                    list = list,
                    subtasks = subtasks,
                    subnotes = subnotes,
                    backgroundColor = if (listWidgetConfig.widgetColor == null)
                            GlanceTheme.colors.primaryContainer
                        else
                            ColorProvider(Color(listWidgetConfig.widgetColor?:Color.White.toArgb())),
                    textColor = if (listWidgetConfig.widgetColor == null)
                            GlanceTheme.colors.onPrimaryContainer
                        else
                        ColorProvider(if(UiUtil.isDarkColor(Color(listWidgetConfig.widgetColor?:Color.Black.toArgb()))) Color.White else Color.Black),
                    entryColor = if (listWidgetConfig.widgetColor == null)
                            GlanceTheme.colors.surface
                        else
                            ColorProvider(Color(listWidgetConfig.widgetColorEntries?:Color.Unspecified.toArgb())),
                    entryTextColor = if (listWidgetConfig.widgetColor == null)
                            GlanceTheme.colors.onSurface
                        else
                            ColorProvider(if(UiUtil.isDarkColor(Color(listWidgetConfig.widgetColorEntries?:Color.White.toArgb()))) Color.White else Color.Black),
                    entryTextCancelledColor = if (listWidgetConfig.widgetColor == null)
                        GlanceTheme.colors.onSurfaceVariant
                    else
                        ColorProvider(if(UiUtil.isDarkColor(Color(listWidgetConfig.widgetColorEntries?:Color.White.toArgb()))) Color.White else Color.Black),
                    entryHeaderTextColor = if (listWidgetConfig.widgetColor == null)
                            GlanceTheme.colors.onSurface
                        else
                            ColorProvider(if(UiUtil.isDarkColor(Color(listWidgetConfig.widgetColorEntries?:Color.White.toArgb()))) Color.White else Color.Black),
                    onCheckedChange = { iCalObjectId, checked ->
                        val settingsStateHolder = SettingsStateHolder(context)
                        val keepInSync = settingsStateHolder.settingKeepStatusProgressCompletedInSync.value
                        val linkProgress = settingsStateHolder.settingLinkProgressToSubtasks.value

                        scope.launch(Dispatchers.IO) {
                            //val iCalObject = database.getICalObjectByIdSync(iCalObjectId) ?: return@launch
                            database.updateProgress(
                                id = iCalObjectId,
                                newPercent = if(checked) null else 100,
                                settingKeepStatusProgressCompletedInSync = keepInSync,
                                settingLinkProgressToSubtasks = linkProgress
                            )
                            if(!checked) {
                                NotificationManagerCompat.from(context).cancel(iCalObjectId.toInt())
                                database.setAlarmNotification(iCalObjectId, false)
                            }
                            NotificationPublisher.scheduleNextNotifications(context)
                            SyncUtil.notifyContentObservers(context)
                        }
                    },
                    onOpenWidgetConfig = {
                        val intent = Intent(context, ListWidgetConfigActivity::class.java).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, GlanceAppWidgetManager(context).getAppWidgetId(id))
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    onAddNew = {
                        val addNewIntent = Intent(context, MainActivity2::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            action = when (listWidgetConfig.module) {
                                Module.JOURNAL -> MainActivity2.INTENT_ACTION_ADD_JOURNAL
                                Module.NOTE -> MainActivity2.INTENT_ACTION_ADD_NOTE
                                Module.TODO -> MainActivity2.INTENT_ACTION_ADD_TODO
                            }
                            listWidgetConfig.searchCollection.firstOrNull()?.let {
                                putExtra(MainActivity2.INTENT_EXTRA_COLLECTION2PRESELECT, it)
                            }
                            val categoriesArray = arrayListOf<String>()
                            categoriesArray.addAll(listWidgetConfig.defaultCategories)
                            putStringArrayListExtra(MainActivity2.INTENT_EXTRA_CATEGORIES2PRESELECT, categoriesArray)
                        }
                        context.startActivity(addNewIntent)
                    },
                    onOpenFilteredList = {
                        val openFilteredListIntent = Intent(context, MainActivity2::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            action = MainActivity2.INTENT_ACTION_OPEN_FILTERED_LIST
                            putExtra(MainActivity2.INTENT_EXTRA_LISTWIDGETCONFIG, Json.encodeToString(listWidgetConfig))
                        }
                        context.startActivity(openFilteredListIntent)
                    }
                )
            }
        }
    }
}
