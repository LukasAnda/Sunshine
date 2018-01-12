/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.data

import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import com.example.android.sunshine.data.TestUtilities.BULK_INSERT_RECORDS_TO_INSERT
import com.example.android.sunshine.data.TestUtilities.createBulkInsertTestWeatherValues
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail

/**
 * Although these tests aren't a complete set of tests one should run on a ContentProvider
 * implementation, they do test that the basic functionality of Sunshine's ContentProvider is
 * working properly.
 *
 *
 * In this test suite, we have the following tests:
 *
 *
 * 1) A test to ensure that your ContentProvider has been properly registered in the
 * AndroidManifest
 *
 *
 * 2) A test to determine if you've implemented the query functionality for your
 * ContentProvider properly
 *
 *
 * 3) A test to determine if you've implemented the bulkInsert functionality of your
 * ContentProvider properly
 *
 *
 * 4) A test to determine if you've implemented the delete functionality of your
 * ContentProvider properly.
 *
 *
 * If any of these tests fail, you should see useful error messages in the testing console's
 * output window.
 *
 *
 * Finally, we have a method annotated with the @Before annotation, which tells the test runner
 * that the [.setUp] method should be called before every method annotated with a @Test
 * annotation. In our setUp method, all we do is delete all records from the database to start our
 * tests with a clean slate each time.
 */
@RunWith(AndroidJUnit4::class)
class TestWeatherProvider {

    /* Context used to access various parts of the system */
    private val mContext = InstrumentationRegistry.getTargetContext()

    /**
     * Because we annotate this method with the @Before annotation, this method will be called
     * before every single method with an @Test annotation. We want to start each test clean, so we
     * delete all entries in the weather table to do so.
     */
    @Before
    fun setUp() {
        deleteAllRecordsFromWeatherTable()
    }

    /**
     * This test checks to make sure that the content provider is registered correctly in the
     * AndroidManifest file. If it fails, you should check the AndroidManifest to see if you've
     * added a <provider></provider> tag and that you've properly specified the android:authorities attribute.
     *
     *
     * Potential causes for failure:
     *
     *
     * 1) Your WeatherProvider was registered with the incorrect authority
     *
     *
     * 2) Your WeatherProvider was not registered at all
     */
    @Test
    fun testProviderRegistry() {

        /*
         * A ComponentName is an identifier for a specific application component, such as an
         * Activity, ContentProvider, BroadcastReceiver, or a Service.
         *
         * Two pieces of information are required to identify a component: the package (a String)
         * it exists in, and the class (a String) name inside of that package.
         *
         * We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        val packageName = mContext.packageName
        val weatherProviderClassName = WeatherProvider::class.java.name
        val componentName = ComponentName(packageName, weatherProviderClassName)

        try {

            /*
             * Get a reference to the package manager. The package manager allows us to access
             * information about packages installed on a particular device. In this case, we're
             * going to use it to get some information about our ContentProvider under test.
             */
            val pm = mContext.packageManager

            /* The ProviderInfo will contain the authority, which is what we want to test */
            val providerInfo = pm.getProviderInfo(componentName, 0)
            val actualAuthority = providerInfo.authority
            val expectedAuthority = WeatherContract.CONTENT_AUTHORITY

            /* Make sure that the registered authority matches the authority from the Contract */
            val incorrectAuthority = "Error: WeatherProvider registered with authority: " + actualAuthority +
                    " instead of expected authority: " + expectedAuthority
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority)

        } catch (e: PackageManager.NameNotFoundException) {
            val providerNotRegisteredAtAll = "Error: WeatherProvider not registered at " + mContext.packageName
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll)
        }

    }

    /**
     * This test uses the database directly to insert a row of test data and then uses the
     * ContentProvider to read out the data. We access the database directly to insert the data
     * because we are testing our ContentProvider's query functionality. If we wanted to use the
     * ContentProvider's insert method, we would have to assume that that insert method was
     * working, which defeats the point of testing.
     *
     *
     * If this test fails, you should check the logic in your
     * [WeatherProvider.insert] and make sure it matches up with our
     * solution code.
     *
     *
     * Potential causes for failure:
     *
     *
     * 1) There was a problem inserting data into the database directly via SQLite
     *
     *
     * 2) The values contained in the cursor did not match the values we inserted via SQLite
     */
    @Test
    fun testBasicWeatherQuery() {

        /* Use WeatherDbHelper to get access to a writable database */
        val dbHelper = WeatherDbHelper(mContext)
        val database = dbHelper.writableDatabase

        /* Obtain weather values from TestUtilities */
        val testWeatherValues = TestUtilities.createTestWeatherContentValues()

        /* Insert ContentValues into database and get a row ID back */
        val weatherRowId = database.insert(
                /* Table to insert values into */
                WeatherContract.WeatherEntry.TABLE_NAME, null,
                /* Values to insert into table */
                testWeatherValues)

        val insertFailed = "Unable to insert into the database"
        assertTrue(insertFailed, weatherRowId != -1)

        /* We are done with the database, close it now. */
        database.close()

        /*
         * Perform our ContentProvider query. We expect the cursor that is returned will contain
         * the exact same data that is in testWeatherValues and we will validate that in the next
         * step.
         */
        val weatherCursor = mContext.contentResolver.query(
                WeatherContract.WeatherEntry.CONTENT_URI, null, null, null, null)/* Columns; leaving this null returns every column in the table *//* Optional specification for columns in the "where" clause above *//* Values for "where" clause *//* Sort order to return in Cursor */

        /* This method will ensure that we  */
        TestUtilities.validateThenCloseCursor("testBasicWeatherQuery",
                weatherCursor!!,
                testWeatherValues)
    }

    /**
     * This test test the bulkInsert feature of the ContentProvider. It also verifies that
     * registered ContentObservers receive onChange callbacks when data is inserted.
     *
     *
     * It finally queries the ContentProvider to make sure that the table has been successfully
     * inserted.
     *
     *
     * Potential causes for failure:
     *
     *
     * 1) Within [WeatherProvider.delete], you didn't call
     * getContext().getContentResolver().notifyChange(uri, null) after performing an insertion.
     *
     *
     * 2) The number of records the ContentProvider reported that it inserted do no match the
     * number of records we inserted into the ContentProvider.
     *
     *
     * 3) The size of the Cursor returned from the query does not match the number of records
     * that we inserted into the ContentProvider.
     *
     *
     * 4) The data contained in the Cursor from our query does not match the data we inserted
     * into the ContentProvider.
     *
     */
    @Test
    fun testBulkInsert() {

        /* Create a new array of ContentValues for weather */
        val bulkInsertTestContentValues = INSTANCE.createBulkInsertTestWeatherValues()

        /*
         * TestContentObserver allows us to test weather or not notifyChange was called
         * appropriately. We will use that here to make sure that notifyChange is called when a
         * deletion occurs.
         */
        val weatherObserver = TestUtilities.testContentObserver

        /*
         * A ContentResolver provides us access to the content model. We can use it to perform
         * deletions and queries at our CONTENT_URI
         */
        val contentResolver = mContext.contentResolver

        /* Register a content observer to be notified of changes to data at a given URI (weather) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                WeatherContract.WeatherEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                weatherObserver)

        /* bulkInsert will return the number of records that were inserted. */
        val insertCount = contentResolver.bulkInsert(
                /* URI at which to insert data */
                WeatherContract.WeatherEntry.CONTENT_URI,
                /* Array of values to insert into given URI */
                bulkInsertTestContentValues)

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        weatherObserver.waitForNotificationOrFail()

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(weatherObserver)

        /*
         * We expect that the number of test content values that we specify in our TestUtility
         * class were inserted here. We compare that value to the value that the ContentProvider
         * reported that it inserted. These numbers should match.
         */
        val expectedAndActualInsertedRecordCountDoNotMatch = "Number of expected records inserted does not match actual inserted record count"
        assertEquals(expectedAndActualInsertedRecordCountDoNotMatch,
                insertCount,
                INSTANCE.getBULK_INSERT_RECORDS_TO_INSERT())

        /*
         * Perform our ContentProvider query. We expect the cursor that is returned will contain
         * the exact same data that is in testWeatherValues and we will validate that in the next
         * step.
         */
        val cursor = mContext.contentResolver.query(
                WeatherContract.WeatherEntry.CONTENT_URI, null, null, null,
                /* Sort by date from smaller to larger (past to future) */
                WeatherContract.WeatherEntry.COLUMN_DATE + " ASC")/* Columns; leaving this null returns every column in the table *//* Optional specification for columns in the "where" clause above *//* Values for "where" clause */

        /*
         * Although we already tested the number of records that the ContentProvider reported
         * inserting, we are now testing the number of records that the ContentProvider actually
         * returned from the query above.
         */
        assertEquals(cursor!!.count, INSTANCE.getBULK_INSERT_RECORDS_TO_INSERT())

        /*
         * We now loop through and validate each record in the Cursor with the expected values from
         * bulkInsertTestContentValues.
         */
        cursor.moveToFirst()
        var i = 0
        while (i < INSTANCE.getBULK_INSERT_RECORDS_TO_INSERT()) {
            TestUtilities.validateCurrentRecord(
                    "testBulkInsert. Error validating WeatherEntry " + i,
                    cursor,
                    bulkInsertTestContentValues[i])
            i++
            cursor.moveToNext()
        }

        /* Always close the Cursor! */
        cursor.close()
    }

    /**
     * This test deletes all records from the weather table using the ContentProvider. It also
     * verifies that registered ContentObservers receive onChange callbacks when data is deleted.
     *
     *
     * It finally queries the ContentProvider to make sure that the table has been successfully
     * cleared.
     *
     *
     * NOTE: This does not delete the table itself. It just deletes the rows of data contained
     * within the table.
     *
     *
     * Potential causes for failure:
     *
     *
     * 1) Within [WeatherProvider.delete], you didn't call
     * getContext().getContentResolver().notifyChange(uri, null) after performing a deletion.
     *
     *
     * 2) The cursor returned from the query was null
     *
     *
     * 3) After the attempted deletion, the ContentProvider still provided weather data
     */
    @Test
    fun testDeleteAllRecordsFromProvider() {

        /*
         * Ensure there are records to delete from the database. Due to our setUp method, the
         * database will not have any records in it prior to this method being run.
         */
        testBulkInsert()

        /*
         * TestContentObserver allows us to test weather or not notifyChange was called
         * appropriately. We will use that here to make sure that notifyChange is called when a
         * deletion occurs.
         */
        val weatherObserver = TestUtilities.testContentObserver

        /*
         * A ContentResolver provides us access to the content model. We can use it to perform
         * deletions and queries at our CONTENT_URI
         */
        val contentResolver = mContext.contentResolver

        /* Register a content observer to be notified of changes to data at a given URI (weather) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                WeatherContract.WeatherEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                weatherObserver)

        /* Delete all of the rows of data from the weather table */
        contentResolver.delete(
                WeatherContract.WeatherEntry.CONTENT_URI, null, null)/* Columns; leaving this null returns every column in the table *//* Optional specification for columns in the "where" clause above */

        /* Perform a query of the data that we've just deleted. This should be empty. */
        val shouldBeEmptyCursor = contentResolver.query(
                WeatherContract.WeatherEntry.CONTENT_URI, null, null, null, null)/* Columns; leaving this null returns every column in the table *//* Optional specification for columns in the "where" clause above *//* Values for "where" clause *//* Sort order to return in Cursor */

        /*
         * If this fails, it's likely you didn't call notifyChange in your delete method from
         * your ContentProvider.
         */
        weatherObserver.waitForNotificationOrFail()

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(weatherObserver)

        /* In some cases, the cursor can be null. That's actually a failure case here. */
        val cursorWasNull = "Cursor was null."
        assertNotNull(cursorWasNull, shouldBeEmptyCursor)

        /* If the count of the cursor is not zero, all records weren't deleted */
        val allRecordsWereNotDeleted = "Error: All records were not deleted from weather table during delete"
        assertEquals(allRecordsWereNotDeleted,
                0,
                shouldBeEmptyCursor!!.count)

        /* Always close your cursor */
        shouldBeEmptyCursor.close()
    }

    /**
     * This method will clear all rows from the weather table in our database.
     *
     *
     * Please note:
     *
     *
     * - This does NOT delete the table itself. We call this method from our @Before annotated
     * method to clear all records from the database before each test on the ContentProvider.
     *
     *
     * - We don't use the ContentProvider's delete functionality to perform this row deletion
     * because in this class, we are attempting to test the ContentProvider. We can't assume
     * that our ContentProvider's delete method works in our ContentProvider's test class.
     */
    private fun deleteAllRecordsFromWeatherTable() {
        /* Access writable database through WeatherDbHelper */
        val helper = WeatherDbHelper(InstrumentationRegistry.getTargetContext())
        val database = helper.writableDatabase

        /* The delete method deletes all of the desired rows from the table, not the table itself */
        database.delete(WeatherContract.WeatherEntry.TABLE_NAME, null, null)

        /* Always close the database when you're through with it */
        database.close()
    }
}