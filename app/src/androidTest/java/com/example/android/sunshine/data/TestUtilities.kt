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

import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_DATE
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_DEGREES
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_HUMIDITY
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_MAX_TEMP
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_MIN_TEMP
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_PRESSURE
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_WEATHER_ID
import com.example.android.sunshine.data.WeatherContract.WeatherEntry.Companion.COLUMN_WIND_SPEED
import com.example.android.sunshine.utilities.SunshineDateUtils
import com.example.android.sunshine.utils.PollingCheck
import junit.framework.Assert.*
import java.lang.reflect.Modifier
import java.util.regex.Pattern

/**
 * These are functions and some test data to make it easier to test your database and Content
 * Provider.
 *
 *
 * NOTE: If your WeatherContract class doesn't exactly match ours, THIS WILL NOT WORK as we've
 * provided and you will need to make changes to this code to use it to pass your tests.
 */
internal object TestUtilities {

    /* October 1st, 2016 at midnight, GMT time */
    val DATE_NORMALIZED = 1475280000000L

    val BULK_INSERT_RECORDS_TO_INSERT = 10


    val testContentObserver: TestContentObserver
        get() = TestContentObserver.testContentObserver

    /**
     * Ensures there is a non empty cursor and validates the cursor's data by checking it against
     * a set of expected values. This method will then close the cursor.
     *
     * @param error          Message when an error occurs
     * @param valueCursor    The Cursor containing the actual values received from an arbitrary query
     * @param expectedValues The values we expect to receive in valueCursor
     */
    fun validateThenCloseCursor(error: String, valueCursor: Cursor, expectedValues: ContentValues) {
        assertNotNull(
                "This cursor is null. Did you make sure to register your ContentProvider in the manifest?",
                valueCursor)

        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst())
        validateCurrentRecord(error, valueCursor, expectedValues)
        valueCursor.close()
    }

    /**
     * This method iterates through a set of expected values and makes various assertions that
     * will pass if our app is functioning properly.
     *
     * @param error          Message when an error occurs
     * @param valueCursor    The Cursor containing the actual values received from an arbitrary query
     * @param expectedValues The values we expect to receive in valueCursor
     */
    fun validateCurrentRecord(error: String, valueCursor: Cursor, expectedValues: ContentValues) {
        val valueSet = expectedValues.valueSet()

        for ((columnName, value) in valueSet) {
            val index = valueCursor.getColumnIndex(columnName)

            /* Test to see if the column is contained within the cursor */
            val columnNotFoundError = "Column '$columnName' not found. $error"
            assertFalse(columnNotFoundError, index == -1)

            /* Test to see if the expected value equals the actual value (from the Cursor) */
            val expectedValue = value.toString()
            val actualValue = valueCursor.getString(index)

            val valuesDontMatchError = ("Actual value '" + actualValue
                    + "' did not match the expected value '" + expectedValue + "'. "
                    + error)

            assertEquals(valuesDontMatchError,
                    expectedValue,
                    actualValue)
        }
    }

    /**
     * Used as a convenience method to return a singleton instance of ContentValues to populate
     * our database or insert using our ContentProvider.
     *
     * @return ContentValues that can be inserted into our ContentProvider or weather.db
     */
    fun createTestWeatherContentValues(): ContentValues {

        val testWeatherValues = ContentValues()

        testWeatherValues.put(COLUMN_DATE, DATE_NORMALIZED)
        testWeatherValues.put(COLUMN_DEGREES, 1.1)
        testWeatherValues.put(COLUMN_HUMIDITY, 1.2)
        testWeatherValues.put(COLUMN_PRESSURE, 1.3)
        testWeatherValues.put(COLUMN_MAX_TEMP, 75)
        testWeatherValues.put(COLUMN_MIN_TEMP, 65)
        testWeatherValues.put(COLUMN_WIND_SPEED, 5.5)
        testWeatherValues.put(COLUMN_WEATHER_ID, 321)

        return testWeatherValues
    }

    /**
     * Used as a convenience method to return a singleton instance of an array of ContentValues to
     * populate our database or insert using our ContentProvider's bulk insert method.
     *
     *
     * It is handy to have utility methods that produce test values because it makes it easy to
     * compare results from ContentProviders and databases to the values you expect to receive.
     * See [.validateCurrentRecord] and
     * [.validateThenCloseCursor] for more information on how
     * this verification is performed.
     *
     * @return Array of ContentValues that can be inserted into our ContentProvider or weather.db
     */
    fun createBulkInsertTestWeatherValues(): Array<ContentValues?> {

        val bulkTestWeatherValues = arrayOfNulls<ContentValues>(BULK_INSERT_RECORDS_TO_INSERT)

        val testDate = TestUtilities.DATE_NORMALIZED
        var normalizedTestDate = SunshineDateUtils.normalizeDate(testDate)

        for (i in 0 until BULK_INSERT_RECORDS_TO_INSERT) {

            normalizedTestDate += SunshineDateUtils.DAY_IN_MILLIS

            val weatherValues = ContentValues()

            weatherValues.put(COLUMN_DATE, normalizedTestDate)
            weatherValues.put(COLUMN_DEGREES, 1.1)
            weatherValues.put(COLUMN_HUMIDITY, 1.2 + 0.01 * i.toFloat())
            weatherValues.put(COLUMN_PRESSURE, 1.3 - 0.01 * i.toFloat())
            weatherValues.put(COLUMN_MAX_TEMP, 75 + i)
            weatherValues.put(COLUMN_MIN_TEMP, 65 - i)
            weatherValues.put(COLUMN_WIND_SPEED, 5.5 + 0.2 * i.toFloat())
            weatherValues.put(COLUMN_WEATHER_ID, 321)

            bulkTestWeatherValues[i] = weatherValues
        }

        return bulkTestWeatherValues
    }

    /**
     * Students: The functions we provide inside of TestWeatherProvider use TestContentObserver to test
     * the ContentObserver callbacks using the PollingCheck class from the Android Compatibility
     * Test Suite tests.
     *
     *
     * NOTE: This only tests that the onChange function is called; it DOES NOT test that the
     * correct Uri is returned.
     */
    internal class TestContentObserver private constructor(val mHT: HandlerThread) : ContentObserver(Handler(mHT.looper)) {
        var mContentChanged: Boolean = false

        /**
         * Called when a content change occurs.
         *
         *
         * To ensure correct operation on older versions of the framework that did not provide a
         * Uri argument, applications should also implement this method whenever they implement
         * the [.onChange] overload.
         *
         * @param selfChange True if this is a self-change notification.
         */
        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        /**
         * Called when a content change occurs. Includes the changed content Uri when available.
         *
         * @param selfChange True if this is a self-change notification.
         * @param uri        The Uri of the changed content, or null if unknown.
         */
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            mContentChanged = true
        }

        /**
         * Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
         * It's useful to look at the Android CTS source for ideas on how to test your Android
         * applications. The reason that PollingCheck works is that, by default, the JUnit testing
         * framework is not running on the main Android application thread.
         */
        fun waitForNotificationOrFail() {

            object : PollingCheck(5000) {
                override fun check(): Boolean {
                    return mContentChanged
                }
            }.run()
            mHT.quit()
        }

        companion object {

            val testContentObserver: TestContentObserver
                get() {
                    val ht = HandlerThread("ContentObserverThread")
                    ht.start()
                    return TestContentObserver(ht)
                }
        }
    }

    fun getConstantNameByStringValue(klass: Class<*>, value: String): String? {
        for (f in klass.declaredFields) {
            val modifiers = f.modifiers
            val type = f.type
            val isPublicStaticFinalString = (Modifier.isStatic(modifiers)
                    && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)
                    && type.isAssignableFrom(String::class.java))

            if (isPublicStaticFinalString) {
                val fieldName = f.name
                try {
                    val fieldValue = klass.getDeclaredField(fieldName).get(null) as String
                    if (fieldValue == value) return fieldName
                } catch (e: IllegalAccessException) {
                    return null
                } catch (e: NoSuchFieldException) {
                    return null
                }

            }
        }

        return null
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getStaticStringField(clazz: Class<*>, variableName: String): String {
        val stringField = clazz.getDeclaredField(variableName)
        stringField.isAccessible = true
        return stringField.get(null) as String
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getStaticIntegerField(clazz: Class<*>, variableName: String): Int? {
        val intField = clazz.getDeclaredField(variableName)
        intField.isAccessible = true
        return intField.get(null) as Int
    }

    fun studentReadableClassNotFound(e: ClassNotFoundException): String {
        val message = e.message
        val indexBeforeSimpleClassName = message?.lastIndexOf('.')
        var simpleClassNameThatIsMissing = message?.substring(indexBeforeSimpleClassName?.plus(1)!!)
        simpleClassNameThatIsMissing = simpleClassNameThatIsMissing?.replace("\\$".toRegex(), ".")
        return ("Couldn't find the class "
                + simpleClassNameThatIsMissing
                + ".\nPlease make sure you've created that class and followed the TODOs.")
    }

    fun studentReadableNoSuchField(e: NoSuchFieldException): String {
        val message = e.message

        val p = Pattern.compile("No field (\\w*) in class L.*/(\\w*\\$?\\w*);")

        val m = p.matcher(message)

        if (m.find()) {
            val missingFieldName = m.group(1)
            val classForField = m.group(2).replace("\\$".toRegex(), ".")
            return ("Couldn't find "
                    + missingFieldName + " in class " + classForField + "."
                    + "\nPlease make sure you've declared that field and followed the TODOs.")
        } else {
            return e.message.toString()
        }
    }
}