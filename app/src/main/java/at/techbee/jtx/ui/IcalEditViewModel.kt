/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import at.techbee.jtx.addLongToCSVString
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.fortuna.ical4j.model.Recur
import java.util.*


class IcalEditViewModel(
    val iCalEntity: ICalEntity,
    val database: ICalDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        const val TAB_GENERAL = 0
        const val TAB_PEOPLE_RES = 1
        const val TAB_LOC_COMMENTS = 2
        const val TAB_ATTACHMENTS = 3
        const val TAB_SUBTASKS = 4
        const val TAB_ALARMS = 5
        const val TAB_RECURRING = 6

        const val RECURRENCE_MODE_UNSUPPORTED = -1
        const val RECURRENCE_MODE_DAY = 0
        const val RECURRENCE_MODE_WEEK = 1
        const val RECURRENCE_MODE_MONTH = 2
        const val RECURRENCE_MODE_YEAR = 3
    }

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allResources: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<ICalCollection>>
    lateinit var allRelatedto: LiveData<List<Relatedto>>

    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>

    var recurrenceList = mutableListOf<Long>()

    var returnIcalObjectId: MutableLiveData<Long> =
        MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)
    var deleteClicked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)
    var collectionNotFoundError: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)

    var iCalObjectUpdated: MutableLiveData<ICalObject> =
        MutableLiveData<ICalObject>().apply { postValue(iCalEntity.property) }

    var categoryUpdated: MutableList<Category> = mutableListOf()
    var commentUpdated: MutableList<Comment> = mutableListOf()
    var attachmentUpdated: MutableList<Attachment> = mutableListOf()
    var attendeeUpdated: MutableList<Attendee> = mutableListOf()
    var resourceUpdated: MutableList<Resource> = mutableListOf()
    var subtaskUpdated: MutableList<ICalObject> = mutableListOf()
    var subtaskDeleted: MutableList<ICalObject> = mutableListOf()


    var possibleTimezones: MutableList<String> =
        mutableListOf("").also { it.addAll(TimeZone.getAvailableIDs().toList()) }

    var collectionVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var summaryVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var descriptionVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var dateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var addTimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timezoneVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var statusVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var classificationVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var urlVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var locationVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var contactVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var categoriesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var attendeesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var resourcesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var commentsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var attachmentsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var takePhotoVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var progressVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var priorityVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var subtasksVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var duedateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var duetimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var completeddateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var completedtimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var starteddateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var startedtimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var addStartedAndDueTimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceGeneralVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceWeekdaysVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceDayOfMonthVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceExceptionsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceAdditionsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()


    var addTimeChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.dueTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.completedTimezone != ICalObject.TZ_ALLDAY)
    var addTimezoneJournalChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VJOURNAL.name && ((iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dtstartTimezone?.isNotEmpty() == true)))
    var addTimezoneTodoChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VTODO.name && ((iCalEntity.property.dueTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dueTimezone?.isNotEmpty() == true) || (iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dtstartTimezone?.isNotEmpty() == true) || (iCalEntity.property.completedTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.completedTimezone?.isNotEmpty() == true)))

    var recurrenceChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.rrule?.isNotBlank())
    //todo make test, pre-fill
    var recurrenceMode: MutableLiveData<Int> = MutableLiveData<Int>(
        try {
            if (iCalEntity.property.rrule.isNullOrEmpty())
                RECURRENCE_MODE_DAY
            else {
                when ((Recur(iCalEntity.property.rrule).frequency)) {
                    Recur.Frequency.YEARLY -> RECURRENCE_MODE_YEAR
                    Recur.Frequency.MONTHLY -> RECURRENCE_MODE_MONTH
                    Recur.Frequency.WEEKLY -> RECURRENCE_MODE_WEEK
                    Recur.Frequency.DAILY -> RECURRENCE_MODE_DAY
                    else -> RECURRENCE_MODE_UNSUPPORTED
                }
            }
        } catch (e: Exception) {
            Log.w("LoadRRule", "Failed to preset UI according to provided RRule\n$e")
            RECURRENCE_MODE_DAY
        })


    val urlError = MutableLiveData<String?>()
    val attendeesError = MutableLiveData<String?>()

    var selectedTab = TAB_GENERAL


    init {

        updateVisibility()

        viewModelScope.launch {

            relatedSubtasks = database.getRelatedTodos(iCalEntity.property.id)

            allCategories = database.getAllCategories()
            allResources = database.getAllResources()
            allCollections = when (iCalEntity.property.component) {
                Component.VTODO.name -> database.getAllVTODOCollections()
                Component.VJOURNAL.name -> database.getAllVJOURNALCollections()
                else -> database.getAllCollections() // should not happen!
            }

            allRelatedto = database.getAllRelatedto()

        }
    }

    fun updateVisibility() {

        collectionVisible.postValue(selectedTab == TAB_GENERAL)
        summaryVisible.postValue(selectedTab == TAB_GENERAL)
        descriptionVisible.postValue(selectedTab == TAB_GENERAL)
        dateVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL))
        timeVisible.postValue((iCalObjectUpdated.value?.dtstart != null && iCalObjectUpdated.value?.dtstartTimezone != ICalObject.TZ_ALLDAY) || (iCalObjectUpdated.value?.due != null && iCalObjectUpdated.value?.dueTimezone != ICalObject.TZ_ALLDAY) || (iCalObjectUpdated.value?.completed != null && iCalObjectUpdated.value?.completedTimezone != ICalObject.TZ_ALLDAY) )
        addTimeVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL))
        timezoneVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL) && iCalObjectUpdated.value?.dtstartTimezone != ICalObject.TZ_ALLDAY ) // simplified IF: Show time only if.module == JOURNAL and Timezone is NOT ALLDAY
        statusVisible.postValue(selectedTab == TAB_GENERAL)
        classificationVisible.postValue(selectedTab == TAB_GENERAL)
        urlVisible.postValue(selectedTab == TAB_LOC_COMMENTS)
        locationVisible.postValue(selectedTab == TAB_LOC_COMMENTS)
        categoriesVisible.postValue(selectedTab == TAB_GENERAL)
        contactVisible.postValue(selectedTab == TAB_PEOPLE_RES)
        attendeesVisible.postValue(selectedTab == TAB_PEOPLE_RES)
        resourcesVisible.postValue(selectedTab == TAB_PEOPLE_RES)
        commentsVisible.postValue(selectedTab == TAB_LOC_COMMENTS)
        attachmentsVisible.postValue(selectedTab == TAB_ATTACHMENTS)
        takePhotoVisible.postValue(selectedTab == TAB_ATTACHMENTS)
        progressVisible.postValue(iCalEntity.property.module == Module.TODO.name && (selectedTab == TAB_GENERAL))
        priorityVisible.postValue(iCalEntity.property.module == Module.TODO.name && (selectedTab == TAB_GENERAL))
        subtasksVisible.postValue(selectedTab == TAB_SUBTASKS)
        duedateVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name)
        duetimeVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name && iCalEntity.property.dueTimezone != ICalObject.TZ_ALLDAY)
        completeddateVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name)
        completedtimeVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name && iCalEntity.property.completedTimezone != ICalObject.TZ_ALLDAY)
        starteddateVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name)
        startedtimeVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name && iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY)
        addStartedAndDueTimeVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name)
        recurrenceVisible.postValue((selectedTab == TAB_RECURRING))
        recurrenceGeneralVisible.postValue((selectedTab == TAB_RECURRING && recurrenceChecked.value?:false))
        recurrenceWeekdaysVisible.postValue((selectedTab == TAB_RECURRING && recurrenceChecked.value?:false && recurrenceMode.value == RECURRENCE_MODE_WEEK))
        recurrenceDayOfMonthVisible.postValue((selectedTab == TAB_RECURRING && recurrenceChecked.value?:false && recurrenceMode.value == RECURRENCE_MODE_MONTH))
        recurrenceExceptionsVisible.postValue(selectedTab == TAB_RECURRING && iCalEntity.property.exdate?.isNotEmpty() == true)
        recurrenceAdditionsVisible.postValue(selectedTab == TAB_RECURRING && iCalEntity.property.rdate?.isNotEmpty() == true)
    }

    fun savingClicked() {
        savingClicked.value = true
    }

    fun deleteClicked() {
        deleteClicked.value = true
    }

    fun clearDates() {
        iCalObjectUpdated.value!!.dtstart = null
        iCalObjectUpdated.value!!.due = null
        iCalObjectUpdated.value!!.completed = null
        addTimezoneTodoChecked.postValue(false)
        addTimeChecked.postValue(false)
        iCalObjectUpdated.postValue(iCalObjectUpdated.value)
    }



    fun update() {
        var insertedOrUpdatedItemId: Long

        //TODO: check if the item got a new sequence in the meantime!

        iCalObjectUpdated.value!!.lastModified = System.currentTimeMillis()
        iCalObjectUpdated.value!!.dtstamp = System.currentTimeMillis()
        iCalObjectUpdated.value!!.sequence++

        if(iCalObjectUpdated.value!!.duration != null)
            iCalObjectUpdated.value!!.duration = null     // we make sure that the unsupported duration is set to null, the user was warned before

        if (iCalObjectUpdated.value!!.collectionId != 1L)
            iCalObjectUpdated.value!!.dirty = true

        if(iCalObjectUpdated.value!!.percent == 0)
            iCalObjectUpdated.value!!.percent = null

        viewModelScope.launch {

            try {
                insertedOrUpdatedItemId = insertOrUpdateICalObject()
                iCalObjectUpdated.value!!.id = insertedOrUpdatedItemId
            } catch (e: SQLiteConstraintException) {
                collectionNotFoundError.value = true
                savingClicked.value = false
                return@launch
            }

            // the case that an item gets deleted at the same time the user was already editing this item, is currently not handled.
            // On save the user would not get an error, he would return to the overview with the deleted item missing


            // do the rest in a background thread
            viewModelScope.launch(Dispatchers.IO) {

                // delete the list attributes, then insert again the once that are still in the list (or were added)
                database.deleteCategories(insertedOrUpdatedItemId)
                database.deleteComments(insertedOrUpdatedItemId)
                database.deleteAttachments(insertedOrUpdatedItemId)
                database.deleteAttendees(insertedOrUpdatedItemId)
                database.deleteResources(insertedOrUpdatedItemId)

                subtaskDeleted.forEach { subtask2del ->
                    viewModelScope.launch(Dispatchers.IO) {
                        database.deleteRelatedChildren(subtask2del.id)       // Also Child-Elements of Child-Elements need to be deleted!
                        database.delete(subtask2del)
                        database.deleteRelatedto(
                            iCalObjectUpdated.value!!.id,
                            subtask2del.id
                        )
                    }
                    Log.println(Log.INFO, "Subtask", "${subtask2del.summary} deleted")

                }

                // now insert or update the item and take care of all attributes
                // insert new Categories
                categoryUpdated.forEach { newCategory ->
                    newCategory.icalObjectId = insertedOrUpdatedItemId
                    database.insertCategory(newCategory)
                }
                commentUpdated.forEach { newComment ->
                    newComment.icalObjectId =
                        insertedOrUpdatedItemId                    //Update the foreign key for newly added comments
                    database.insertComment(newComment)
                }
                attachmentUpdated.forEach { newAttachment ->
                    newAttachment.icalObjectId =
                        insertedOrUpdatedItemId                    //Update the foreign key for newly added attachments
                    database.insertAttachment(newAttachment)
                }
                attendeeUpdated.forEach { newAttendee ->
                    newAttendee.icalObjectId = insertedOrUpdatedItemId
                    database.insertAttendee(newAttendee)
                }
                resourceUpdated.forEach { newResource ->
                    newResource.icalObjectId = insertedOrUpdatedItemId
                    database.insertResource(newResource)
                }
                subtaskUpdated.forEach { subtask ->
                    subtask.sequence++
                    subtask.lastModified = System.currentTimeMillis()
                    subtask.dirty = true
                    subtask.collectionId = iCalObjectUpdated.value?.collectionId!!
                    when(subtask.percent) {
                        in 1..99 -> subtask.status = StatusTodo.`IN-PROCESS`.name
                        100 -> subtask.status = StatusTodo.COMPLETED.name
                        else -> subtask.status = StatusTodo.`NEEDS-ACTION`.name
                    }
                    if(subtask.percent == 0)
                        subtask.percent = null
                    subtask.id = database.insertSubtask(subtask)
                    Log.println(Log.INFO, "Subtask", "${subtask.id} ${subtask.summary} added")


                    // upsert relation from the Parent to the Child

                    // check if the relation is there, if not we insert
                    val childRelation = database.findRelatedTo(insertedOrUpdatedItemId, subtask.id, Reltype.CHILD.name)
                    if(childRelation == null) {
                        database.insertRelatedto(
                            Relatedto(
                                icalObjectId = insertedOrUpdatedItemId,
                                linkedICalObjectId = subtask.id,
                                reltype = Reltype.CHILD.name,
                                text = subtask.uid
                            )
                        )
                    }

                    // upsert relation from the Child to the Parent
                    // check if the relation is there, if not we insert
                    val parentRelation = database.findRelatedTo(subtask.id, insertedOrUpdatedItemId, Reltype.PARENT.name)
                    if(parentRelation == null) {
                        database.insertRelatedto(
                            Relatedto(
                                icalObjectId = subtask.id,
                                linkedICalObjectId = insertedOrUpdatedItemId,
                                reltype = Reltype.PARENT.name,
                                text = iCalObjectUpdated.value!!.uid
                            )
                        )
                    }

                }

                if (recurrenceList.size > 0 || iCalObjectUpdated.value!!.id != 0L)    // recreateRecurring if the recurrenceList is not empty, but also when it is an update, as the recurrence might have been deactivated and it is necessary to delete instances
                    launch(Dispatchers.IO) {
                        iCalObjectUpdated.value?.recreateRecurring(database)
                    }


                if (iCalObjectUpdated.value?.recurOriginalIcalObjectId != null && iCalObjectUpdated.value?.isRecurLinkedInstance == false) {
                    viewModelScope.launch(Dispatchers.IO) {

                        val newExceptionList = addLongToCSVString(
                            database.getRecurExceptions(iCalObjectUpdated.value?.recurOriginalIcalObjectId!!),
                            iCalObjectUpdated.value!!.dtstart
                        )

                        database.setRecurExceptions(
                            iCalObjectUpdated.value?.recurOriginalIcalObjectId!!,
                            newExceptionList,
                            System.currentTimeMillis()
                        )
                    }
                }
            }

            returnIcalObjectId.value = insertedOrUpdatedItemId
        }
    }

    private suspend fun insertOrUpdateICalObject(): Long {
        return when {
            iCalObjectUpdated.value!!.id == 0L -> {

                //Log.println(Log.INFO, "VJournalItemViewModel", "creating a new one")
                database.insertICalObject(iCalObjectUpdated.value!!)
                //Log.println(Log.INFO, "vJournalItemViewModel", vJournalItemUpdate.id.toString())
            }
            iCalEntity.ICalCollection!!.collectionId != iCalObjectUpdated.value!!.collectionId -> {

                val oldId = iCalEntity.property.id

                val newId = updateCollectionWithChildren(iCalObjectUpdated.value!!.id, null)

                // possible changes must still be updated
                iCalObjectUpdated.value!!.id = newId
                database.update(iCalObjectUpdated.value!!)

                // once the newId is there, the local entries can be deleted (or marked as deleted)
                viewModelScope.launch(Dispatchers.IO) {
                    ICalObject.deleteItemWithChildren(oldId, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
                }

                return newId
            }
            else -> {
                database.update(iCalObjectUpdated.value!!)
                iCalObjectUpdated.value!!.id
            }
        }
    }


    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            ICalObject.deleteItemWithChildren(
                iCalObjectUpdated.value!!.id,
                database
            )
        }
    }


    /**
     * @param [id] the id of the item for which the collection needs to be updated
     * @param [parentId] is needed for the recursive call in order to provide it for the movItemToNewCollection(...) function. For the initial call this would be null as the function should initially always be called from the top parent.
     *
     * this function takes care of
     * 1. moving the item to a new collection (by copying and deleting the current item)
     * 2. determining the children of this item and calling itself recusively to to the same again for each child.
     *
     * @return The new id of the item in the new collection
     */
    private suspend fun updateCollectionWithChildren(id: Long, parentId: Long?): Long {

        val newParentId = moveItemToNewCollection(id, parentId)

        // then determine the children and recursively call the function again. The possible child becomes the new parent and is added to the list until there are no more children.
        val children =
            allRelatedto.value?.filter { it.icalObjectId == id && it.reltype == Reltype.CHILD.name }
        children?.forEach {
            it.linkedICalObjectId?.let { linkedICalObjectId ->
                updateCollectionWithChildren(
                    linkedICalObjectId,
                    newParentId
                )
            }
        }
        return newParentId
    }

    /**
     * @param [id] is the id of the original item that should be moved to another collection. On the recursive call this is the id of the original child.
     * @param [newParentId] is the id of the parent that was already copied into the new collection. This is needed in order to re-create the relation between the parent and the child.
     *
     * This function creates a copy of an item with all it's children in the new collection and then
     * deletes (or marks as deleted) the original item.
     *
     * @return the new id of the item that was inserted (that becomes the newParentId)
     *
     */
    private suspend fun moveItemToNewCollection(id: Long, newParentId: Long?): Long =
        withContext(Dispatchers.IO) {
            val item = database.getSync(id)
            if (item != null) {

                item.property.id = 0L
                item.property.collectionId = iCalObjectUpdated.value!!.collectionId
                item.property.sequence = 0
                item.property.dirty = true
                item.property.lastModified = System.currentTimeMillis()
                item.property.created = System.currentTimeMillis()
                item.property.dtstamp = System.currentTimeMillis()
                item.property.uid = ICalObject.generateNewUID()
                item.property.flags = null
                item.property.scheduleTag = null
                item.property.eTag = null
                item.property.fileName = null

                val newId = database.insertICalObject(item.property)

                item.attendees?.forEach {
                    it.icalObjectId = newId
                    database.insertAttendee(it)
                }

                item.resources?.forEach {
                    it.icalObjectId = newId
                    database.insertResource(it)
                }

                item.categories?.forEach {
                    it.icalObjectId = newId
                    database.insertCategory(it)
                }

                item.comments?.forEach {
                    it.icalObjectId = newId
                    database.insertComment(it)
                }

                if (item.organizer != null) {
                    item.organizer?.icalObjectId = newId
                    database.insertOrganizer(item.organizer!!)
                }

                item.attachments?.forEach {
                    it.icalObjectId = newId
                    database.insertAttachment(it)
                }

                // relations need to be rebuilt from the new parent to the child
                // relations need to be rebuilt from the new child to the parent
                if (newParentId != null) {
                    val relChild2Parent = Relatedto()
                    relChild2Parent.icalObjectId = newParentId
                    relChild2Parent.linkedICalObjectId = newId
                    relChild2Parent.reltype = Reltype.CHILD.name
                    relChild2Parent.text = item.property.uid
                    database.insertRelatedto(relChild2Parent)

                    val parent = database.getSync(newParentId)
                    val relParent2Child = Relatedto()
                    relParent2Child.icalObjectId = newId
                    relParent2Child.linkedICalObjectId = newParentId
                    relParent2Child.reltype = Reltype.PARENT.name
                    relParent2Child.text = parent?.property?.uid
                    database.insertRelatedto(relParent2Child)
                }

                return@withContext newId
            }
            return@withContext 0L
        }


    fun clearUrlError() {
        urlError.value = null
    }

    fun clearAttendeeError() {
        attendeesError.value = null
    }
}

