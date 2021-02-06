package at.bitfire.notesx5.database

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*


/** The name of the the table.  */
const val TABLE_NAME_ICALOBJECT = "icalobject"

/** The name of the ID column.  */
const val COLUMN_ID = BaseColumns._ID

/** The names of all the other columns  */
const val COLUMN_COMPONENT = "component"
const val COLUMN_COLLECTION = "collection"
const val COLUMN_SUMMARY = "summary"
const val COLUMN_DESCRIPTION = "description"
const val COLUMN_DTSTART = "dtstart"
const val COLUMN_DTSTART_TIMEZONE = "dtstarttimezone"
const val COLUMN_DTEND = "dtend"
const val COLUMN_DTEND_TIMEZONE = "dtendtimezone"
const val COLUMN_STATUS = "status"
const val COLUMN_STATUS_X = "statusx"
const val COLUMN_CLASSIFICATION = "classification"
const val COLUMN_CLASSIFICATION_X = "classificationx"
const val COLUMN_URL = "url"
const val COLUMN_CONTACT = "contact"
const val COLUMN_GEO_LAT = "geolat"
const val COLUMN_GEO_LONG = "geolong"
const val COLUMN_LOCATION = "location"
const val COLUMN_PERCENT = "percent"
const val COLUMN_PRIORITY = "priority"
const val COLUMN_PRIORITY_X = "priorityx"
const val COLUMN_DUE = "due"
const val COLUMN_DUE_TIMEZONE = "duetimezone"
const val COLUMN_COMPLETED = "completed"
const val COLUMN_UID = "uid"
const val COLUMN_CREATED = "created"
const val COLUMN_DTSTAMP = "dtstamp"
const val COLUMN_LAST_MODIFIED = "lastmodified"
const val COLUMN_SEQUENCE = "sequence"
const val COLUMN_COLOR = "color"


@Parcelize
@Entity(tableName = TABLE_NAME_ICALOBJECT, indices = [Index(value = ["_id", "summary", "description"])])
data class ICalObject(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_ID)
        var id: Long = 0L,

        @ColumnInfo(name = COLUMN_COMPONENT) var component: String = "NOTE",          // JOURNAL or NOTE
        @ColumnInfo(name = COLUMN_COLLECTION) var collection: String = "LOCAL",
        @ColumnInfo(name = COLUMN_SUMMARY) var summary: String? = null,
        @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String? = null,
        @ColumnInfo(name = COLUMN_DTSTART) var dtstart: Long? = null,
        @ColumnInfo(name = COLUMN_DTSTART_TIMEZONE) var dtstartTimezone: String? = null,

        @ColumnInfo(name = COLUMN_DTEND) var dtend: Long? = null,
        @ColumnInfo(name = COLUMN_DTEND_TIMEZONE) var dtendTimezone: String? = null,

        @ColumnInfo(name = COLUMN_STATUS) var status: Int = 0,     // 0 = DRAFT, 1 = FINAL, 2 = CANCELLED, -1 = NOT SUPPORTED (value in statusX)
        @ColumnInfo(name = COLUMN_STATUS_X) var statusX: String? = null,
        @ColumnInfo(name = COLUMN_CLASSIFICATION) var classification: Int = CLASSIFICATION_PUBLIC,    // 0 = PUBLIC, 1 = PRIVATE, 2 = CONFIDENTIAL, -1 = NOT SUPPORTED (value in classificationX)
        @ColumnInfo(name = COLUMN_CLASSIFICATION_X) var classificationX: String? = null,

        @ColumnInfo(name = COLUMN_URL) var url: String? = null,
        @ColumnInfo(name = COLUMN_CONTACT) var contact: String? = null,
        @ColumnInfo(name = COLUMN_GEO_LAT) var geoLat: Float? = null,
        @ColumnInfo(name = COLUMN_GEO_LONG) var geoLong: Float? = null,
        @ColumnInfo(name = COLUMN_LOCATION) var location: String? = null,

        @ColumnInfo(name = COLUMN_PERCENT) var percent: Int? = null,    // VTODO only!
        @ColumnInfo(name = COLUMN_PRIORITY) var priority: Int? = null,   // VTODO and VEVENT
        @ColumnInfo(name = COLUMN_PRIORITY_X) var priorityX: String? = null,

        @ColumnInfo(name = COLUMN_DUE) var due: Long? = null,      // VTODO only!
        @ColumnInfo(name = COLUMN_DUE_TIMEZONE) var dueTimezone: String? = null, //VTODO only!
        @ColumnInfo(name = COLUMN_COMPLETED) var completed: Long? = null, // VTODO only!


        @ColumnInfo(name = COLUMN_UID) var uid: String = "${System.currentTimeMillis()}-${UUID.randomUUID()}@at.bitfire.notesx5",                              //unique identifier, see https://tools.ietf.org/html/rfc5545#section-3.8.4.7

        /*
         The following properties specify change management information in  calendar components.
         https://tools.ietf.org/html/rfc5545#section-3.8.7
         */
        @ColumnInfo(name = COLUMN_CREATED) var created: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.1
        @ColumnInfo(name = COLUMN_DTSTAMP) var dtstamp: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.2
        @ColumnInfo(name = COLUMN_LAST_MODIFIED) var lastModified: Long = System.currentTimeMillis(), // see https://tools.ietf.org/html/rfc5545#section-3.8.7.3
        @ColumnInfo(name = COLUMN_SEQUENCE) var sequence: Long = 0,                             // increase on every change (+1), see https://tools.ietf.org/html/rfc5545#section-3.8.7.4

        //var exdate: Long? = System.currentTimeMillis(),   //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.1
        //var rdate: Long? = System.currentTimeMillis()     //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.2
        //var recurrenceId: String? = null,                          //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5
        //var rrule: String?,                               //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.3


        //var ianaProperty: String,
        //var xProperty: String,

        //var vAlarm: String?,
        //var vTimezone: Long?,
        //var ianaComponent: String?,
        //var xComponent: String?

        @ColumnInfo(name = COLUMN_COLOR) var color: String? = null

): Parcelable

{
        companion object Factory {

                const val STATUS_TODO_OPEN = 1

                const val STATUS_JOURNAL_DRAFT = 0
                const val STATUS_JOURNAL_FINAL = 1
                const val STATUS_JOURNAL_CANCELLED = 2

                const val STATUS_TODO_NEEDSACTION = 0
                const val STATUS_TODO_COMPLETED = 2
                const val STATUS_TODO_INPROCESS = 1
                const val STATUS_TODO_CANCELLED = 3

                const val CLASSIFICATION_PUBLIC = 0
                const val CLASSIFICATION_PRIVATE = 1
                const val CLASSIFICATION_CONFIDENTIAL = 2



                fun createJournal(): ICalObject = ICalObject(component = "JOURNAL", dtstart = System.currentTimeMillis(), status = STATUS_JOURNAL_FINAL)
                fun createNote(): ICalObject = ICalObject(component = "NOTE", status = STATUS_JOURNAL_FINAL)
                fun createNote(summary: String) = ICalObject(component = "NOTE", status = STATUS_JOURNAL_FINAL, summary = summary)
                fun createTodo() = ICalObject(component = "TODO", status = STATUS_TODO_NEEDSACTION, percent = 0, priority = 0, dueTimezone = "ALLDAY")
                fun createSubtask(summary: String) = ICalObject(component = "TODO", summary = summary, status = 0, percent = 0, dueTimezone = "ALLDAY")


                /**
                 * Create a new [ICalObject] from the specified [ContentValues].
                 *
                 * @param values A [ICalObject] that at least contain [.COLUMN_NAME].
                 * @return A newly created [ICalObject] instance.
                 */
                fun fromContentValues(values: ContentValues?): ICalObject? {

                        // TODO initialize specific component based on values!
                        // TODO validate some inputs, especially Int Inputs!
                        val iCalObject = ICalObject()
                        if (values == null)
                                return null

                        if (values.containsKey(COLUMN_COMPONENT)) {
                                iCalObject.component = values.getAsString(COLUMN_COMPONENT)
                        }
                        if (values.containsKey(COLUMN_COLLECTION)) {
                                iCalObject.collection = values.getAsString(COLUMN_COLLECTION)
                        }
                        if (values.containsKey(COLUMN_SUMMARY)) {
                                iCalObject.summary = values.getAsString(COLUMN_SUMMARY)
                        }
                        if (values.containsKey(COLUMN_DTSTART)) {
                                iCalObject.dtstart = values.getAsLong(COLUMN_DTSTART)
                        }
                        if (values.containsKey(COLUMN_DTSTART_TIMEZONE)) {
                                iCalObject.dtstartTimezone = values.getAsString(COLUMN_DTSTART_TIMEZONE)
                        }
                        if (values.containsKey(COLUMN_DTEND)) {
                                iCalObject.dtend = values.getAsLong(COLUMN_DTEND)
                        }
                        if (values.containsKey(COLUMN_DTEND_TIMEZONE)) {
                                iCalObject.dtendTimezone = values.getAsString(COLUMN_DTEND_TIMEZONE)
                        }

                        if (values.containsKey(COLUMN_STATUS)) {
                                iCalObject.status = values.getAsInteger(COLUMN_STATUS)
                        }
                        if (values.containsKey(COLUMN_STATUS_X)) {
                                iCalObject.statusX = values.getAsString(COLUMN_STATUS_X)
                        }
                        if (values.containsKey(COLUMN_CLASSIFICATION)) {
                                iCalObject.classification = values.getAsInteger(COLUMN_CLASSIFICATION)
                        }
                        if (values.containsKey(COLUMN_CLASSIFICATION_X)) {
                                iCalObject.classificationX = values.getAsString(COLUMN_CLASSIFICATION_X)
                        }
                        if (values.containsKey(COLUMN_URL)) {
                                iCalObject.url = values.getAsString(COLUMN_URL)
                        }
                        if (values.containsKey(COLUMN_CONTACT)) {
                                iCalObject.contact = values.getAsString(COLUMN_CONTACT)
                        }
                        if (values.containsKey(COLUMN_GEO_LAT)) {
                                iCalObject.geoLat = values.getAsFloat(COLUMN_GEO_LAT)
                        }
                        if (values.containsKey(COLUMN_GEO_LONG)) {
                                iCalObject.geoLong = values.getAsFloat(COLUMN_GEO_LONG)
                        }
                        if (values.containsKey(COLUMN_LOCATION)) {
                                iCalObject.location = values.getAsString(COLUMN_LOCATION)
                        }
                        if (values.containsKey(COLUMN_PERCENT)) {
                                iCalObject.percent = values.getAsInteger(COLUMN_PERCENT)
                        }
                        if (values.containsKey(COLUMN_PRIORITY)) {
                                iCalObject.priority = values.getAsInteger(COLUMN_PRIORITY)
                        }
                        if (values.containsKey(COLUMN_PRIORITY_X)) {
                                iCalObject.priorityX = values.getAsString(COLUMN_PRIORITY_X)
                        }
                        if (values.containsKey(COLUMN_DUE)) {
                                iCalObject.due = values.getAsLong(COLUMN_DUE)
                        }
                        if (values.containsKey(COLUMN_DUE_TIMEZONE)) {
                                iCalObject.dueTimezone = values.getAsString(COLUMN_DUE_TIMEZONE)
                        }
                        if (values.containsKey(COLUMN_COMPLETED)) {
                                iCalObject.completed = values.getAsLong(COLUMN_COMPLETED)
                        }
                        if (values.containsKey(COLUMN_UID)) {
                                iCalObject.uid = values.getAsString(COLUMN_UID)
                        }
                        if (values.containsKey(COLUMN_CREATED)) {
                                iCalObject.created = values.getAsLong(COLUMN_CREATED)
                        }
                        if (values.containsKey(COLUMN_DTSTAMP)) {
                                iCalObject.dtstamp = values.getAsLong(COLUMN_DTSTAMP)
                        }
                        if (values.containsKey(COLUMN_LAST_MODIFIED)) {
                                iCalObject.lastModified = values.getAsLong(COLUMN_LAST_MODIFIED)
                        }
                        if (values.containsKey(COLUMN_SEQUENCE)) {
                                iCalObject.sequence = values.getAsLong(COLUMN_SEQUENCE)
                        }
                        if (values.containsKey(COLUMN_COLOR)) {
                                iCalObject.color = values.getAsString(COLUMN_COLOR)
                        }

                        return ICalObject()

                }


                fun applyContentValues(values: ContentValues?) {


                }

        }



}

