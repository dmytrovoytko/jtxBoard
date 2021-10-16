/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.JtxContract.asSyncAdapter
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.TABLE_NAME_ICALOBJECT
import at.techbee.jtx.database.properties.Reltype
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class SyncContentProviderTest {

    private var mContentResolver: ContentResolver? = null

    private var defaultTestAccount = Account("testAccount", "testAccount")
    private var defaultCollectionUri: Uri? = null
    private var defaultCollectionId: Long? = null
    private var defaultICalObjectUri: Uri? = null
    private var defaultICalObjectId: Long? = null


    @Before
    fun setUp() {
        //val context: Context = ApplicationProvider.getApplicationContext()
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        ICalDatabase.switchToInMemory(context)
        mContentResolver = context.contentResolver

        //prepare
        defaultCollectionUri = insertCollection(defaultTestAccount, null, null)
        defaultCollectionId = defaultCollectionUri?.lastPathSegment?.toLongOrNull()

        defaultICalObjectUri = insertIcalObject(defaultTestAccount, "journal4attendee", defaultCollectionId!!)
        defaultICalObjectId = defaultICalObjectUri?.lastPathSegment?.toLongOrNull()
    }

    @After
    fun tearDown() {

        //cleanup
        mContentResolver?.delete(defaultCollectionUri!!, null, null)
        defaultCollectionUri = null
        defaultCollectionId = null
        defaultICalObjectId = null
        defaultICalObjectUri = null
    }



    @Test
    fun icalObject_insert_find_delete()  {

        //insert
        val newICalObject = insertIcalObject(defaultTestAccount, "note2delete", defaultCollectionId!!)

        //find
        mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY), null, null, null).use {
            assertEquals(1, it!!.count)
        }

        //update
        val updatedContentValues = ContentValues()
        updatedContentValues.put(JtxContract.JtxICalObject.DESCRIPTION, "description was updated")
        val countUpdated = mContentResolver?.update(newICalObject!!, updatedContentValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "icalObject_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        //find
        mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY, JtxContract.JtxICalObject.DESCRIPTION), "${JtxContract.JtxICalObject.SUMMARY} = ?", arrayOf("note2delete"), null).use {
            assertEquals(1, it!!.count)
        }
        //delete
        val countDel: Int? = mContentResolver?.delete(newICalObject!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY, JtxContract.JtxICalObject.DESCRIPTION), "${JtxContract.JtxICalObject.SUMMARY} = ?", arrayOf("note2delete"), null).use {
            assertEquals(0, it!!.count)
        }
    }


    @Test
    fun attendee_insert_find_update_delete()  {

        // INSERT a new Attendee
        val attendeeValues = ContentValues().apply {
            put(JtxContract.JtxAttendee.ICALOBJECT_ID, defaultICalObjectId)
            put(JtxContract.JtxAttendee.CALADDRESS, "mailto:test@test.com")
        }
        val newAttendee = mContentResolver?.insert(JtxContract.JtxAttendee.CONTENT_URI.asSyncAdapter(defaultTestAccount), attendeeValues)
        assertNotNull(newAttendee)

        //QUERY the Attendee
        mContentResolver?.query(newAttendee!!, arrayOf(JtxContract.JtxAttendee.ID), null, null, null)!!.use {
            assertEquals(1, it.count)             // inserted object was found
        }

        // UPDATE the new value
        val updatedAttendeeValues = ContentValues(1).apply {
            put(JtxContract.JtxAttendee.CALADDRESS, "mailto:test@test.net")
        }
        val countUpdated = mContentResolver?.update(newAttendee!!, updatedAttendeeValues, null, null)
        assertEquals(countUpdated, 1)

        // QUERY the updated value
        mContentResolver?.query(newAttendee!!, arrayOf(JtxContract.JtxAttendee.ID, JtxContract.JtxAttendee.CALADDRESS), "${JtxContract.JtxAttendee.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)!!.use {
            assertEquals(1, it.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newAttendee!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newAttendee!!, arrayOf(JtxContract.JtxAttendee.ID, JtxContract.JtxAttendee.CALADDRESS), "${JtxContract.JtxAttendee.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)!!.use {
            assertEquals(0, it.count)             // inserted object was found
        }
    }



    @Test
    fun category_insert_find_update_delete()  {

        // INSERT
        val categoryValues = ContentValues().apply {
            put(JtxContract.JtxCategory.ICALOBJECT_ID, defaultICalObjectId)
            put(JtxContract.JtxCategory.TEXT, "inserted category")
        }
        val uriCategories = JtxContract.JtxCategory.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newCategory = mContentResolver?.insert(uriCategories, categoryValues)
        assertNotNull(newCategory)

        //QUERY
        mContentResolver?.query(newCategory!!, arrayOf(JtxContract.JtxCategory.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedCategoryValues = ContentValues()
        updatedCategoryValues.put(JtxContract.JtxCategory.TEXT, "updated category")
        val countUpdated = mContentResolver?.update(newCategory!!, updatedCategoryValues, null, null)
        assertEquals(1, countUpdated)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newCategory!!, arrayOf(JtxContract.JtxCategory.ID, JtxContract.JtxCategory.TEXT), "${JtxContract.JtxCategory.TEXT} = ?", arrayOf("updated category"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newCategory!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newCategory!!, arrayOf(JtxContract.JtxCategory.ID, JtxContract.JtxCategory.TEXT), "${JtxContract.JtxCategory.TEXT} = ?", arrayOf("updated category"), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }




    @Test
    fun comment_insert_find_update_delete()  {

        // INSERT
        val commentValues = ContentValues()
        commentValues.put(JtxContract.JtxComment.ICALOBJECT_ID, defaultICalObjectId)
        commentValues.put(JtxContract.JtxComment.TEXT, "inserted comment")
        val uriComments = JtxContract.JtxComment.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newComment = mContentResolver?.insert(uriComments, commentValues)
        assertNotNull(newComment)

        //QUERY
        mContentResolver?.query(newComment!!, arrayOf(JtxContract.JtxComment.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedCommentValues = ContentValues()
        updatedCommentValues.put(JtxContract.JtxComment.TEXT, "updated comment")
        val countUpdated = mContentResolver?.update(newComment!!, updatedCommentValues, null, null)
        assertEquals(1, countUpdated)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newComment!!, arrayOf(JtxContract.JtxComment.ID, JtxContract.JtxComment.TEXT), "${JtxContract.JtxComment.TEXT} = ?", arrayOf("updated comment"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newComment!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newComment!!, arrayOf(JtxContract.JtxComment.ID, JtxContract.JtxComment.TEXT), "${JtxContract.JtxComment.TEXT} = ?", arrayOf("updated comment"), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }

    }




    @Test
    fun contact_insert_find_update_delete()  {

       // INSERT
        val contactValues = ContentValues()
        contactValues.put(JtxContract.JtxContact.ICALOBJECT_ID, defaultICalObjectId)
        contactValues.put(JtxContract.JtxContact.TEXT, "inserted contact")
        val uriContacts = JtxContract.JtxContact.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newContact = mContentResolver?.insert(uriContacts, contactValues)
        assertNotNull(newContact)

        //QUERY
        mContentResolver?.query(newContact!!, arrayOf(JtxContract.JtxContact.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedContactValues = ContentValues()
        updatedContactValues.put(JtxContract.JtxContact.TEXT, "updated contact")
        val countUpdated = mContentResolver?.update(newContact!!, updatedContactValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newContact!!, arrayOf(JtxContract.JtxContact.ID, JtxContract.JtxContact.TEXT), "${JtxContract.JtxContact.TEXT} = ?", arrayOf("updated contact"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newContact!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newContact!!, arrayOf(JtxContract.JtxContact.ID, JtxContract.JtxContact.TEXT), "${JtxContract.JtxContact.TEXT} = ?", arrayOf("updated contact"), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }


    @Test
    fun organizer_insert_find_update_delete()  {

        // INSERT
        val contactValues = ContentValues()
        contactValues.put(JtxContract.JtxOrganizer.ICALOBJECT_ID, defaultICalObjectId)
        contactValues.put(JtxContract.JtxOrganizer.CALADDRESS, "mailto:a@b.com")
        val uriOrganizer = JtxContract.JtxOrganizer.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newOrganizer = mContentResolver?.insert(uriOrganizer, contactValues)
        assertNotNull(newOrganizer)

        //QUERY
        mContentResolver?.query(newOrganizer!!, arrayOf(JtxContract.JtxOrganizer.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedOrganizerValues = ContentValues()
        updatedOrganizerValues.put(JtxContract.JtxOrganizer.CALADDRESS, "mailto:c@d.com")
        val countUpdated = mContentResolver?.update(newOrganizer!!, updatedOrganizerValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newOrganizer!!, arrayOf(JtxContract.JtxOrganizer.ID, JtxContract.JtxOrganizer.CALADDRESS), "${JtxContract.JtxOrganizer.CALADDRESS} = ?", arrayOf("mailto:c@d.com"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newOrganizer!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newOrganizer!!, arrayOf(JtxContract.JtxOrganizer.ID, JtxContract.JtxOrganizer.CALADDRESS), "${JtxContract.JtxOrganizer.CALADDRESS} = ?", arrayOf("mailto:c@d.com"), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }



    @Test
    fun relatedto_insert_find_update_delete()  {

        // INSERT
        val relatedtoValues = ContentValues()
        relatedtoValues.put(JtxContract.JtxRelatedto.ICALOBJECT_ID, defaultICalObjectId)
        relatedtoValues.put(JtxContract.JtxRelatedto.LINKEDICALOBJECT_ID, defaultICalObjectId)
        relatedtoValues.put(JtxContract.JtxRelatedto.RELTYPE, Reltype.CHILD.name)
        val uriRelatedto = JtxContract.JtxRelatedto.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newRelatedto = mContentResolver?.insert(uriRelatedto, relatedtoValues)
        assertNotNull(newRelatedto)

        //QUERY
        mContentResolver?.query(newRelatedto!!, arrayOf(JtxContract.JtxRelatedto.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }             // inserted object was found

        // UPDATE
        val updatedRelatedtoValues = ContentValues()
        updatedRelatedtoValues.put(JtxContract.JtxRelatedto.RELTYPE, "Parent")
        val countUpdated = mContentResolver?.update(newRelatedto!!, updatedRelatedtoValues, null, null)
        assertEquals(1, countUpdated)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newRelatedto!!, arrayOf(JtxContract.JtxRelatedto.ID, JtxContract.JtxRelatedto.ICALOBJECT_ID), "${JtxContract.JtxRelatedto.ICALOBJECT_ID} = ?", arrayOf(defaultICalObjectId.toString()), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newRelatedto!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newRelatedto!!, arrayOf(JtxContract.JtxRelatedto.ID, JtxContract.JtxRelatedto.ICALOBJECT_ID), "${JtxContract.JtxRelatedto.ICALOBJECT_ID} = ?", arrayOf(defaultICalObjectId.toString()), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }



    @Test
    fun resource_insert_find_update_delete()  {

        // INSERT
        val values = ContentValues().apply {
            put(JtxContract.JtxResource.ICALOBJECT_ID, defaultICalObjectId)
            put(JtxContract.JtxResource.TEXT, "inserted resource")
        }
        val uriResource = JtxContract.JtxResource.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newResource = mContentResolver?.insert(uriResource, values)
        assertNotNull(newResource)

        //QUERY
        mContentResolver?.query(newResource!!, arrayOf(JtxContract.JtxResource.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedResourceValues = ContentValues()
        updatedResourceValues.put(JtxContract.JtxResource.TEXT, "updated resource")
        val countUpdated = mContentResolver?.update(newResource!!, updatedResourceValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newResource!!, arrayOf(JtxContract.JtxResource.ID, JtxContract.JtxResource.TEXT), "${JtxContract.JtxResource.TEXT} = ?", arrayOf("updated resource"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newResource!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newResource!!, arrayOf(JtxContract.JtxResource.ID, JtxContract.JtxResource.TEXT), "${JtxContract.JtxResource.TEXT} = ?", arrayOf("updated resource"), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }


    @Test
    fun attachment_insert_find_update_delete()  {

        // INSERT
        val values = ContentValues().apply {
            put(JtxContract.JtxAttachment.ICALOBJECT_ID, defaultICalObjectId)
            put(JtxContract.JtxAttachment.URI, "content://at.techbee.jtx.fileprovider/jtx_images/jtx_6028748326614733966.aac")
            put(JtxContract.JtxAttachment.BINARY, "AAAA")
        }
        val uriAttachment = JtxContract.JtxAttachment.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newAttachment = mContentResolver?.insert(uriAttachment, values)
        assertNotNull(newAttachment)

        //QUERY
        mContentResolver?.query(newAttachment!!, arrayOf(JtxContract.JtxAttachment.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedValues = ContentValues()
        updatedValues.put(JtxContract.JtxAttachment.URI, "content://at.techbee.jtx.fileprovider/jtx_files/1631560872968.aac")
        val countUpdated = mContentResolver?.update(newAttachment!!, updatedValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newAttachment!!, arrayOf(JtxContract.JtxAttachment.ID, JtxContract.JtxAttachment.URI), "${JtxContract.JtxAttachment.URI} = ?", arrayOf("content://at.techbee.jtx.fileprovider/jtx_files/1631560872968.aac"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newAttachment!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newAttachment!!, arrayOf(JtxContract.JtxAttachment.ID, JtxContract.JtxAttachment.URI), "${JtxContract.JtxAttachment.URI} = ?", arrayOf("https://techbee.at"), null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }


    @Test
    fun alarm_insert_find_update_delete()  {

        // INSERT
        val values = ContentValues().apply {
            put(JtxContract.JtxAlarm.ICALOBJECT_ID, defaultICalObjectId)
            put(JtxContract.JtxAlarm.ALARM_VALUE, "BEGIN:VALARM\n" +
                    "TRIGGER;VALUE=DATE-TIME:19970317T133000Z\n" +
                    "REPEAT:4\n" +
                    "DURATION:PT15M\n" +
                    "ACTION:AUDIO\n" +
                    "ATTACH;FMTTYPE=audio/basic:ftp://example.com/pub/\n" +
                    " sounds/bell-01.aud\n" +
                    "END:VALARM")
        }
        val uriAlarm = JtxContract.JtxAlarm.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newAlarm = mContentResolver?.insert(uriAlarm, values)
        assertNotNull(newAlarm)

        //QUERY
        mContentResolver?.query(newAlarm!!, arrayOf(JtxContract.JtxAlarm.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedValues = ContentValues()
        updatedValues.put(JtxContract.JtxAlarm.ALARM_VALUE, "BEGIN:VALARM\n" +
                "TRIGGER;VALUE=DATE-TIME:19970317T133000Z\n" +
                "REPEAT:5\n" +
                "DURATION:PT15M\n" +
                "ACTION:AUDIO\n" +
                "ATTACH;FMTTYPE=audio/basic:ftp://example.com/pub/\n" +
                " sounds/bell-01.aud\n" +
                "END:VALARM")
        val countUpdated = mContentResolver?.update(newAlarm!!, updatedValues, null, null)
        assertEquals(countUpdated, 1)

        // QUERY the updated value
        mContentResolver?.query(newAlarm!!, arrayOf(JtxContract.JtxAlarm.ID, JtxContract.JtxAlarm.ALARM_VALUE), "${JtxContract.JtxAlarm.ALARM_VALUE} = ?", arrayOf("BEGIN:VALARM\n" +
                "TRIGGER;VALUE=DATE-TIME:19970317T133000Z\n" +
                "REPEAT:5\n" +
                "DURATION:PT15M\n" +
                "ACTION:AUDIO\n" +
                "ATTACH;FMTTYPE=audio/basic:ftp://example.com/pub/\n" +
                " sounds/bell-01.aud\n" +
                "END:VALARM"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newAlarm!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newAlarm!!, arrayOf(JtxContract.JtxAlarm.ID), null, null, null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }



    @Test
    fun unknown_insert_find_update_delete()  {

        // INSERT
        val values = ContentValues().apply {
            put(JtxContract.JtxUnknown.ICALOBJECT_ID, defaultICalObjectId)
            put(JtxContract.JtxUnknown.UNKNOWN_VALUE, "[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value2\"}]")
        }
        val uriUknown = JtxContract.JtxUnknown.CONTENT_URI.asSyncAdapter(defaultTestAccount)
        val newUnknown = mContentResolver?.insert(uriUknown, values)
        assertNotNull(newUnknown)

        //QUERY
        mContentResolver?.query(newUnknown!!, arrayOf(JtxContract.JtxUnknown.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE
        val updatedValues = ContentValues()
        updatedValues.put(JtxContract.JtxUnknown.UNKNOWN_VALUE, "[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value3\"}]")
        val countUpdated = mContentResolver?.update(newUnknown!!, updatedValues, null, null)
        assertEquals(countUpdated, 1)

        // QUERY the updated value
        mContentResolver?.query(newUnknown!!, arrayOf(JtxContract.JtxUnknown.ID, JtxContract.JtxUnknown.UNKNOWN_VALUE), "${JtxContract.JtxUnknown.UNKNOWN_VALUE} = ?", arrayOf("[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value3\"}]"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        //delete
        val countDel: Int? = mContentResolver?.delete(newUnknown!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        mContentResolver?.query(newUnknown!!, arrayOf(JtxContract.JtxUnknown.ID), null, null, null).use {
            assertEquals(0, it!!.count)             // inserted object was found
        }
    }


    @Test
    fun collection_insert_find_update_delete()  {

        // INSERT a new Collection
        val newCollection = insertCollection(Account("testcollection", "testcollection"), "testcollection", "testcollection")
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newCollectionUriId", newCollection.toString())
        assertNotNull(newCollectionId)

        //QUERY the Collection
        mContentResolver?.query(newCollection!!, arrayOf(JtxContract.JtxCollection.ID), null, null, null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // UPDATE the new value
        val updatedCollectionValues = ContentValues()
        updatedCollectionValues.put(JtxContract.JtxCollection.DISPLAYNAME, "testcollection updated")
        val countUpdated = mContentResolver?.update(newCollection!!, updatedCollectionValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        mContentResolver?.query(newCollection!!, arrayOf(JtxContract.JtxCollection.ID, JtxContract.JtxCollection.DISPLAYNAME), "${JtxContract.JtxCollection.DISPLAYNAME} = ?", arrayOf("testcollection updated"), null).use {
            assertEquals(1, it!!.count)             // inserted object was found
        }

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newCollection!!, null, null)
        assertEquals(1, countDeleted)

    }

    @Test
    fun collection_query_all() {
        //prepare
        val account = Account("collection1", "collection1")
        val col1 = insertCollection(account, null, "Collection1")
        val col2 = insertCollection(account, null, "Collection2")

        mContentResolver?.query(JtxContract.JtxCollection.CONTENT_URI.asSyncAdapter(account), arrayOf(JtxContract.JtxCollection.ID), null, null, null).use {
            assertEquals(2, it!!.count)             // inserted object was found
        }

        mContentResolver?.delete(col1!!, null, null)
        mContentResolver?.delete(col2!!, null, null)

    }


    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uriInvalid = Uri.parse("content://${JtxContract.AUTHORITY}/invalid")
        mContentResolver?.query(uriInvalid, arrayOf(JtxContract.JtxICalObject.ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uriWrong = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT/asdf")
        mContentResolver?.query(uriWrong, arrayOf(JtxContract.JtxICalObject.ID), null, null, null)
    }


    @Test
    fun check_for_SQL_injection_through_contentValues()  {

        val contentValuesCurrupted = ContentValues()
        contentValuesCurrupted.put(JtxContract.JtxICalObject.SUMMARY, "note2corrupted\"; delete * from $TABLE_NAME_ICALOBJECT")
        contentValuesCurrupted.put(JtxContract.JtxICalObject.ICALOBJECT_COLLECTIONID, defaultCollectionId)
        val newUri2 = mContentResolver?.insert(JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(defaultTestAccount), contentValuesCurrupted)


        mContentResolver?.query(newUri2!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY), null, null, null).use {
            assertEquals(1, it!!.count)
        }
    }


    @Test
    fun check_for_SQL_injection_through_query()  {

        mContentResolver?.query(defaultICalObjectUri!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY), "${JtxContract.JtxICalObject.SUMMARY} = ?); DELETE * FROM $TABLE_NAME_ICALOBJECT", arrayOf("note2check"), null).use {
            assertEquals(0, it!!.count)
        }

        mContentResolver?.query(JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(defaultTestAccount), arrayOf(JtxContract.JtxICalObject.ID), null, null, null).use {
            assertTrue(it!!.count > 0)     // there must be entries! Delete must not be executed!
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_IS_SYNC_ADAPTER()  {

        val queryParamsInavalid = "$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, "note2check")
        mContentResolver?.insert(uri, contentValues)
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_NAME()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, "note2check")
        mContentResolver?.insert(uri, contentValues)
    }


    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_TYPE()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_NAME=test&"
        val uri = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, "note2check")
        mContentResolver?.insert(uri, contentValues)
    }



    private fun insertCollection(account: Account, url: String?, displayname: String?): Uri? {

        // INSERT a new Collection
        val collectionValues = ContentValues().apply {
            put(JtxContract.JtxCollection.DISPLAYNAME, displayname)
            put(JtxContract.JtxCollection.URL, url)
            put(JtxContract.JtxCollection.ACCOUNT_NAME, account.name)
            put(JtxContract.JtxCollection.ACCOUNT_TYPE, account.type)
        }

        val uriCollection = JtxContract.JtxCollection.CONTENT_URI.asSyncAdapter(account)

        return mContentResolver?.insert(uriCollection, collectionValues)
    }


    private fun insertIcalObject(account: Account, summary: String, collectionId: Long): Uri? {
        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, summary)
        contentValues.put(JtxContract.JtxICalObject.ICALOBJECT_COLLECTIONID, collectionId)
        val uriIcalobject = JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(account)

        return mContentResolver?.insert(uriIcalobject, contentValues)

    }




    /*
    @Test
    fun delete() {
    }

    @Test
    fun getType() {
    }

    @Test
    fun insert() {
    }

    @Test
    fun onCreate() {
    }

    @Test
    fun query() {

    }

    @Test
    fun update() {
    }

     */
}