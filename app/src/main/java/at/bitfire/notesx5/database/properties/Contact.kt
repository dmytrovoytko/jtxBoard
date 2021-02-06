package at.bitfire.notesx5.database.properties


import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize


/** The name of the the table.  */
const val TABLE_NAME_CONTACT = "contact"

/** The name of the ID column.  */
const val COLUMN_CONTACT_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_CONTACT_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
const val COLUMN_CONTACT_TEXT = "text"
const val COLUMN_CONTACT_LANGUAGEPARAM = "languageparam"
const val COLUMN_CONTACT_OTHERPARAM = "otherparam"



@Parcelize
@Entity(tableName = TABLE_NAME_CONTACT,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_CONTACT_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Contact (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_CONTACT_ID)
        var contactId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_CONTACT_ICALOBJECT_ID) var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_CONTACT_TEXT)                        var text: String = "",
        @ColumnInfo(name = COLUMN_CONTACT_LANGUAGEPARAM)               var languageparam: String? = null,
        @ColumnInfo(name = COLUMN_CONTACT_OTHERPARAM)                  var otherparam: String? = null

): Parcelable



{
        companion object Factory {

                /**
                 * Create a new [Contact] from the specified [ContentValues].
                 *
                 * @param values A [Contact] that at least contain [COLUMN_CONTACT_TEXT] and [COLUMN_CONTACT_ICALOBJECT_ID]
                 * @return A newly created [Contact] instance.
                 */
                fun fromContentValues(@Nullable values: ContentValues?): Contact? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_CONTACT_ICALOBJECT_ID) == null || values.getAsString(COLUMN_CONTACT_TEXT) == null)     // at least a icalObjectId and text must be given for a Contact!
                                return null

                        val contact = Contact(icalObjectId = values.getAsLong(COLUMN_CONTACT_ICALOBJECT_ID), text = values.getAsString(COLUMN_CONTACT_TEXT))

                        if (values.containsKey(COLUMN_CONTACT_LANGUAGEPARAM)) {
                                contact.languageparam = values.getAsString(COLUMN_CONTACT_LANGUAGEPARAM)
                        }
                        if (values.containsKey(COLUMN_CONTACT_OTHERPARAM)) {
                                contact.otherparam = values.getAsString(COLUMN_CONTACT_OTHERPARAM)
                        }

                        return contact
                }
        }

}



