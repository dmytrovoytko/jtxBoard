/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations

import android.os.Parcelable
import android.util.Log
import androidx.room.Embedded
import androidx.room.Relation
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.JtxContract
import at.techbee.jtx.database.*
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalObject.Factory.TZ_ALLDAY
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Organizer
import at.techbee.jtx.util.Css3Color
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.parcelize.Parcelize
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.data.DefaultParameterFactorySupplier
import net.fortuna.ical4j.data.DefaultPropertyFactorySupplier
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.component.VAlarm
import net.fortuna.ical4j.model.component.VJournal
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.parameter.*
import net.fortuna.ical4j.model.parameter.Role
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.validate.ValidationException
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URISyntaxException
import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.util.TimeZone


@Parcelize
data class ICalEntity(
    @Embedded
    var property: ICalObject = ICalObject(),


    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Comment::class)
    var comments: List<Comment>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Category::class)
    var categories: List<Category>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Attendee::class)
    var attendees: List<Attendee>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Organizer::class)
    var organizer: Organizer? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Relatedto::class)
    var relatedto: List<Relatedto>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
    var resources: List<Resource>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Attachment::class)
    var attachments: List<Attachment>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Alarm::class)
    var alarms: List<Alarm>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Unknown::class)
    var unknown: List<Unknown>? = null,


    @Relation(
        parentColumn = COLUMN_ICALOBJECT_COLLECTIONID,
        entityColumn = COLUMN_COLLECTION_ID,
        entity = at.techbee.jtx.database.ICalCollection::class
    )
    var ICalCollection: ICalCollection? = null

    /*
    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Contact::class)
    var contact: Contact? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
    var resource: List<Resource>? = null

     */

) : Parcelable {


    fun getIcalFormat(): Calendar {

        val ical = Calendar()
        ical.properties += Version.VERSION_2_0
        ical.properties += ProdId("+//IDN techbee.at//jtxBoard")     // TODO to be adapted!


        if (this.property.component == JtxContract.JtxICalObject.Component.VTODO.name) {
            val vTodo = VToDo(true /* generates DTSTAMP */)
            ical.components += vTodo
            val props = vTodo.properties
            addProperties(props)
        } else if (this.property.component == JtxContract.JtxICalObject.Component.VJOURNAL.name) {
            val vJournal = VJournal(true /* generates DTSTAMP */)
            ical.components += vJournal
            val props = vJournal.properties
            addProperties(props)
        }

        alarms?.forEach { alarm ->
            val vAlarm = VAlarm().apply {
                alarm.action?.let { this.action.value = it }
                if(alarm.triggerTime != null && alarm.triggerRelativeDuration.isNullOrEmpty()) {
                    this.trigger.dateTime = DateTime(alarm.triggerTime!!)
                } else if (alarm.triggerTime == null && alarm.triggerRelativeDuration?.isNotEmpty() == true) {
                    this.trigger.duration = java.time.Duration.parse(alarm.triggerRelativeDuration)
                    if(alarm.triggerRelativeTo == AlarmRelativeTo.START.name)
                        this.trigger.parameters.add(Related.START)
                    else
                        this.trigger.parameters.add(Related.END)
                }
                alarm.summary?.let {this.summary.value = it }
                alarm.repeat?.let { this.repeat.value = it }
                alarm.duration?.let { this.duration.value = it }
                alarm.description?.let { this.description.value = it }
                alarm.attach?.let { this.attachment.value = it }
                alarm.other?.let { this.properties.addAll(getXPropertyListFromJson(it)) }
            }
            ical.components += vAlarm
        }


        return ical
    }


    /**
     * Adds properties of this ICalEntity to a Property List
     * Attention! This is currently more or less copy paste from JtxIcalObject in ical4android!
     * @param props: The property list where the properties should be added
     */
    private fun addProperties(props: PropertyList<Property>) {

        val xPropCompletedtimezone = "X-COMPLETEDTIMEZONE"


        this.property.apply {


            uid.let { props += Uid(it) }
            sequence.let { props += Sequence(it.toInt()) }

            created.let {
                props += Created(DateTime(it).apply {
                    this.isUtc = true
                })
            }
            lastModified.let {
                props += LastModified(DateTime(it).apply {
                    this.isUtc = true
                })
            }

            summary?.let { props += Summary(it) }
            description?.let { props += Description(it) }

            location?.let { location ->
                val loc = Location(location)
                locationAltrep?.let { locationAltrep ->
                    loc.parameters.add(AltRep(locationAltrep))
                }
                props += loc
            }
            if (geoLat != null && geoLong != null) {
                props += Geo(geoLat!!.toBigDecimal(), geoLong!!.toBigDecimal())
            }
            color?.let { props += Color(null, Css3Color.nearestMatch(it).name) }
            url?.let {
                try {
                    props += Url(URI(it))
                } catch (e: URISyntaxException) {
                    Log.w("ical4j processing", "Ignoring invalid task URL: $url", e)
                }
            }
            //organizer?.let { props += it }


            classification?.let { props += Clazz(it) }
            status?.let { props += Status(it) }



            dtstart?.let {
                when {
                    dtstartTimezone == TZ_ALLDAY -> props += DtStart(Date(it))
                    dtstartTimezone == TimeZone.getTimeZone("UTC").id -> props += DtStart(DateTime(it).apply {
                        this.isUtc = true
                    })
                    dtstartTimezone.isNullOrEmpty() -> props += DtStart(DateTime(it).apply {
                        this.isUtc = false
                    })
                    else -> {
                        val timezone = TimeZoneRegistryFactory.getInstance().createRegistry()
                            .getTimeZone(dtstartTimezone)
                        val withTimezone = DtStart(DateTime(it))
                        withTimezone.timeZone = timezone
                        props += withTimezone
                    }
                }
            }

            rrule?.let { rrule ->
                props += RRule(rrule)
            }
            recurid?.let { recurid ->
                props += RecurrenceId(recurid)
            }

            rdate?.let { rdateString ->

                when {
                    dtstartTimezone == TZ_ALLDAY -> {
                        val dateListDate = DateList(Value.DATE)
                        getLongListFromString(rdateString).forEach {
                            dateListDate.add(Date(it))
                        }
                        props += RDate(dateListDate)

                    }
                    dtstartTimezone == TimeZone.getTimeZone("UTC").id -> {
                        val dateListDateTime = DateList(Value.DATE_TIME)
                        getLongListFromString(rdateString).forEach {
                            dateListDateTime.add(DateTime(it).apply {
                                this.isUtc = true
                            })
                        }
                        props += RDate(dateListDateTime)
                    }
                    dtstartTimezone.isNullOrEmpty() -> {
                        val dateListDateTime = DateList(Value.DATE_TIME)
                        getLongListFromString(rdateString).forEach {
                            dateListDateTime.add(DateTime(it).apply {
                                this.isUtc = false
                            })
                        }
                        props += RDate(dateListDateTime)
                    }
                    else -> {
                        val dateListDateTime = DateList(Value.DATE_TIME)
                        val timezone = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(dtstartTimezone)
                        getLongListFromString(rdateString).forEach {
                            val withTimezone = DateTime(it)
                            withTimezone.timeZone = timezone
                            dateListDateTime.add(DateTime(withTimezone))
                        }
                        props += RDate(dateListDateTime)
                    }
                }
            }

            exdate?.let { exdateString ->

                when {
                    dtstartTimezone == TZ_ALLDAY -> {
                        val dateListDate = DateList(Value.DATE)
                        getLongListFromString(exdateString).forEach {
                            dateListDate.add(Date(it))
                        }
                        props += ExDate(dateListDate)

                    }
                    dtstartTimezone == TimeZone.getTimeZone("UTC").id -> {
                        val dateListDateTime = DateList(Value.DATE_TIME)
                        getLongListFromString(exdateString).forEach {
                            dateListDateTime.add(DateTime(it).apply {
                                this.isUtc = true
                            })
                        }
                        props += ExDate(dateListDateTime)
                    }
                    dtstartTimezone.isNullOrEmpty() -> {
                        val dateListDateTime = DateList(Value.DATE_TIME)
                        getLongListFromString(exdateString).forEach {
                            dateListDateTime.add(DateTime(it).apply {
                                this.isUtc = false
                            })
                        }
                        props += ExDate(dateListDateTime)
                    }
                    else -> {
                        val dateListDateTime = DateList(Value.DATE_TIME)
                        val timezone = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(dtstartTimezone)
                        getLongListFromString(exdateString).forEach {
                            val withTimezone = DateTime(it)
                            withTimezone.timeZone = timezone
                            dateListDateTime.add(DateTime(withTimezone))
                        }
                        props += ExDate(dateListDateTime)
                    }
                }
            }

            duration?.let {
                val dur = Duration()
                dur.value = it
                props += dur
            }    // TODO: Check how to deal with duration


            if(component == Component.VTODO.name) {
                completed?.let {
                    //Completed is defines as always DateTime! And is always UTC!?

                    props += Completed(DateTime(it))
                }
                completedTimezone?.let {
                    props += XProperty(xPropCompletedtimezone, it)
                }
                percent?.let {
                    props += PercentComplete(it)
                }


                if (priority != null && priority != Priority.UNDEFINED.level)
                    priority?.let {
                        props += Priority(it)
                    }
                else {
                    props += Priority(Priority.UNDEFINED.level)
                }

                due?.let {
                    when {
                        dueTimezone == TZ_ALLDAY -> props += Due(Date(it))
                        dueTimezone == TimeZone.getTimeZone("UTC").id -> props += Due(DateTime(it).apply {
                            this.isUtc = true
                        })
                        dueTimezone.isNullOrEmpty() -> props += Due(DateTime(it).apply {
                            this.isUtc = false
                        })
                        else -> {
                            val timezone = TimeZoneRegistryFactory.getInstance().createRegistry()
                                .getTimeZone(dueTimezone)
                            val withTimezone = Due(DateTime(it))
                            withTimezone.timeZone = timezone
                            props += withTimezone
                        }
                    }
                }
            }

        }

        val categoryTextList = TextList()
        categories?.forEach {
            categoryTextList.add(it.text)
        }
        if (!categoryTextList.isEmpty)
            props += Categories(categoryTextList)


        val resourceTextList = TextList()
        resources?.forEach {
            resourceTextList.add(it.text)
        }
        if (!resourceTextList.isEmpty)
            props += Resources(resourceTextList)


        comments?.forEach { comment ->
            val c = net.fortuna.ical4j.model.property.Comment(comment.text).apply {
                comment.altrep?.let { this.parameters.add(AltRep(it)) }
                comment.language?.let { this.parameters.add(Language(it)) }
                comment.other?.let {
                    val xparams = getXParametersFromJson(it)
                    xparams.forEach { xparam ->
                        this.parameters.add(xparam)
                    }
                }
            }
            props += c
        }


        attendees?.forEach { attendee ->
            val attendeeProp = net.fortuna.ical4j.model.property.Attendee().apply {
                this.calAddress = URI(attendee.caladdress)

                attendee.cn?.let {
                    this.parameters.add(Cn(it))
                }
                attendee.cutype?.let {
                    when {
                        it.equals(CuType.INDIVIDUAL.value, ignoreCase = true) -> this.parameters.add(
                            CuType.INDIVIDUAL)
                        it.equals(CuType.GROUP.value, ignoreCase = true) -> this.parameters.add(
                            CuType.GROUP)
                        it.equals(CuType.ROOM.value, ignoreCase = true) -> this.parameters.add(
                            CuType.ROOM)
                        it.equals(CuType.RESOURCE.value, ignoreCase = true) -> this.parameters.add(
                            CuType.RESOURCE)
                        it.equals(CuType.UNKNOWN.value, ignoreCase = true) -> this.parameters.add(
                            CuType.UNKNOWN)
                        else -> this.parameters.add(CuType.UNKNOWN)
                    }
                }
                attendee.delegatedfrom?.let {
                    this.parameters.add(DelegatedFrom(it))
                }
                attendee.delegatedto?.let {
                    this.parameters.add(DelegatedTo(it))
                }
                attendee.dir?.let {
                    this.parameters.add(Dir(it))
                }
                attendee.language?.let {
                    this.parameters.add(Language(it))
                }
                attendee.member?.let {
                    this.parameters.add(Member(it))
                }
                attendee.partstat?.let {
                    this.parameters.add(PartStat(it))
                }
                attendee.role?.let {
                    this.parameters.add(Role(it))
                }
                attendee.rsvp?.let {
                    this.parameters.add(Rsvp(it))
                }
                attendee.sentby?.let {
                    this.parameters.add(SentBy(it))
                }
                attendee.other?.let {
                    val params = getXParametersFromJson(it)
                    params.forEach { xparam ->
                        this.parameters.add(xparam)
                    }
                }
            }
            props += attendeeProp
            //todo: take care of other attributes for attendees
        }

        unknown?.forEach {
            it.value?.let {  jsonString ->
                props.add(unknownPropertyFromJsonString(jsonString))
            }
        }

        relatedto?.forEach {
            val param: Parameter =
                when (it.reltype) {
                    RelType.CHILD.value -> RelType.CHILD
                    RelType.SIBLING.value -> RelType.SIBLING
                    RelType.PARENT.value -> RelType.PARENT
                    else -> return@forEach
                }
            val parameterList = ParameterList()
            parameterList.add(param)
            props += RelatedTo(parameterList, it.text)
        }
    }


    fun writeIcalOutputStream(os: ByteArrayOutputStream) {
        val ical = getIcalFormat()
        Log.d("iCalFileContent", ical.toString())
        // Corresponds to   ICalendar.softValidate(ical)   in ical4android
        try {
            ical.validate(true)
        } catch (e: ValidationException) {
            if (BuildConfig.DEBUG)
            // debug build, re-throw ValidationException
                throw e
            else
                Log.w("ical4j processing", "iCalendar validation failed - This is only a warning!", e)
        }


        CalendarOutputter(false).output(ical, os)

    }


    /*
    ATTENTION, this is currently copy paste from ical4Android!
     */

    private fun getXParametersFromJson(string: String): List<XParameter> {

        val jsonObject = JSONObject(string)
        val xparamList = mutableListOf<XParameter>()
        for (i in 0 until jsonObject.length()) {
            val names = jsonObject.names() ?: break
            val xparamName = names[i]?.toString() ?: break
            val xparamValue = jsonObject.getString(xparamName).toString()
            if(xparamName.isNotBlank() && xparamValue.isNotBlank()) {
                val xparam = XParameter(xparamName, xparamValue)
                xparamList.add(xparam)
            }
        }
        return xparamList
    }

    private fun getXPropertyListFromJson(string: String): PropertyList<Property> {

        val jsonObject = JSONObject(string)
        val propertyList = PropertyList<Property>()
        for (i in 0 until jsonObject.length()) {
            val names = jsonObject.names() ?: break
            val propertyName = names[i]?.toString() ?: break
            val propertyValue = jsonObject.getString(propertyName).toString()
            if(propertyName.isNotBlank() && propertyValue.isNotBlank()) {
                val prop = XProperty(propertyName, propertyValue)
                propertyList.add(prop)
            }
        }
        return propertyList
    }

    private fun getLongListFromString(string: String): MutableList<Long> {

        val stringList = string.split(",")
        val longList = mutableListOf<Long>()

        stringList.forEach {
            try {
                longList.add(it.toLong())
            } catch (e: NumberFormatException) {
                Log.w("getLongListFromString", "String could not be cast to Long ($it)")
                return@forEach
            }
        }
        return longList
    }

    /**
     * Deserializes a JSON string from an ExtendedProperty value to an ical4j property.
     *
     * @param jsonString JSON representation of an ical4j property
     * @return ical4j property, generated from [jsonString]
     * @throws org.json.JSONException when the input value can't be parsed
     */
    private fun unknownPropertyFromJsonString(jsonString: String): Property {

        val propertyFactorySupplier: List<PropertyFactory<out Property>> = DefaultPropertyFactorySupplier().get()
        val parameterFactorySupplier: List<ParameterFactory<out Parameter>> = DefaultParameterFactorySupplier().get()

        val json = JSONArray(jsonString)
        val name = json.getString(0)
        val value = json.getString(1)

        val builder = PropertyBuilder(propertyFactorySupplier)
            .name(name)
            .value(value)

        json.optJSONObject(2)?.let { jsonParams ->
            for (paramName in jsonParams.keys())
                builder.parameter(
                    ParameterBuilder(parameterFactorySupplier)
                        .name(paramName)
                        .value(jsonParams.getString(paramName))
                        .build()
                )
        }
        return builder.build()
    }


    /**
     * This function creates a copy of the ICalEntity in the selected module.
     * This also applies some transformations, e.g. when a task is copied to a note
     * @param [moduleString] the new module of the copied entity
     * @return The [ICalEntity] in transformed to the new module (or as journal, if the moduleString was faulty)
     */
    fun getIcalEntityCopy(moduleString: String?): ICalEntity {

        /** TODO: the app keeps the reference of the the objects within the iCalEntity to the original objects (despite the copy()).
         * In the worst case a user click on Copy as Task/Note/Journal, the objects get altered, the user is correctly forwarded to the edit fragment.
         * But when he returns through the back button, the object does not get reset, so if he clicks on the edit button for the element
         * the edit screen would open with the already altered values (e.g. the edit fragment of a task would open if he clicked on
         * Copy as Task before
         */
        val newModule = try { Module.valueOf(moduleString ?: "JOURNAL") } catch (e: IllegalArgumentException) { Module.JOURNAL }     // Fallback is Journal, but this should not happen anyway

        val newEntity = ICalEntity()
        newEntity.property = property.copy()
        newEntity.attendees = attendees
        newEntity.resources = resources
        newEntity.categories = categories
        newEntity.alarms = alarms
        newEntity.attachments = attachments
        newEntity.relatedto = relatedto
        newEntity.ICalCollection = ICalCollection
        newEntity.comments = comments
        newEntity.organizer = organizer
        newEntity.unknown = unknown

        return newEntity.apply {

            property.id = 0L
            property.module = newModule.name
            property.dtstamp = System.currentTimeMillis()
            property.created = System.currentTimeMillis()
            property.lastModified = System.currentTimeMillis()
            property.dtend = null
            property.dtendTimezone = null
            property.recurOriginalIcalObjectId = null
            property.isRecurLinkedInstance = false
            property.exdate = null
            property.rdate = null
            property.uid = ICalObject.generateNewUID()
            property.dirty = true

            property.flags = null
            property.scheduleTag = null
            property.eTag = null
            property.fileName = null


            if (newModule == Module.JOURNAL || newModule == Module.NOTE) {
                property.component = Component.VJOURNAL.name

                if (newModule == Module.JOURNAL && property.dtstart == null) {
                    property.dtstart = DateTimeUtils.getTodayAsLong()
                    property.dtstartTimezone = TZ_ALLDAY
                }
                if(newModule == Module.NOTE) {
                    property.dtstart = null
                    property.dtstartTimezone = null
                }
                property.due = null
                property.dueTimezone = null
                property.completed = null
                property.completedTimezone = null
                property.duration = null
                property.priority = null
                property.percent = null

                if(property.status != StatusJournal.FINAL.name || property.status != StatusJournal.DRAFT.name || property.status != StatusJournal.CANCELLED.name)
                    property.status = StatusJournal.FINAL.name

            } else if (newModule == Module.TODO) {
                property.component = Component.VTODO.name
                if(property.status != StatusTodo.COMPLETED.name || property.status != StatusTodo.`IN-PROCESS`.name || property.status != StatusTodo.`NEEDS-ACTION`.name || property.status != StatusTodo.CANCELLED.name)
                    property.status = StatusTodo.`NEEDS-ACTION`.name
            }

            // reset the ids of all list properties to make sure that they get inserted as new ones
            attachments?.forEach { it.attachmentId = 0L }
            attendees?.forEach { it.attendeeId = 0L }
            categories?.forEach { it.categoryId = 0L }
            comments?.forEach { it.commentId = 0L }
            organizer?.organizerId = 0L
            relatedto?.forEach { it.relatedtoId = 0L }
            resources?.forEach { it.resourceId = 0L }
            alarms?.forEach { it.alarmId = 0L }
            unknown?.forEach { it.unknownId = 0L }

        }
    }
}