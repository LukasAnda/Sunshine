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

import android.content.UriMatcher
import android.net.Uri
import android.support.test.runner.AndroidJUnit4

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

import com.example.android.sunshine.data.TestUtilities.getStaticIntegerField
import com.example.android.sunshine.data.TestUtilities.studentReadableNoSuchField
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail

@RunWith(AndroidJUnit4::class)
class TestUriMatcher {

    private var testMatcher: UriMatcher? = null

    @Before
    fun before() {
        try {

            val buildUriMatcher = WeatherProvider::class.java.getDeclaredMethod("buildUriMatcher")
            testMatcher = buildUriMatcher.invoke(WeatherProvider::class.java) as UriMatcher

            REFLECTED_WEATHER_CODE = getStaticIntegerField(
                    WeatherProvider::class.java,
                    weatherCodeVariableName)!!

            REFLECTED_WEATHER_WITH_DATE_CODE = getStaticIntegerField(
                    WeatherProvider::class.java,
                    weatherCodeWithDateVariableName)!!

        } catch (e: NoSuchFieldException) {
            fail(studentReadableNoSuchField(e))
        } catch (e: IllegalAccessException) {
            fail(e.message)
        } catch (e: NoSuchMethodException) {
            val noBuildUriMatcherMethodFound = "It doesn't appear that you have created a method called buildUriMatcher in " + "the WeatherProvider class."
            fail(noBuildUriMatcherMethodFound)
        } catch (e: InvocationTargetException) {
            fail(e.message)
        }

    }

    /**
     * Students: This function tests that your UriMatcher returns the correct integer value for
     * each of the Uri types that our ContentProvider can handle. Uncomment this when you are
     * ready to test your UriMatcher.
     */
    @Test
    fun testUriMatcher() {

        /* Test that the code returned from our matcher matches the expected weather code */
        val weatherUriDoesNotMatch = "Error: The CODE_WEATHER URI was matched incorrectly."
        val actualWeatherCode = testMatcher!!.match(TEST_WEATHER_DIR)
        val expectedWeatherCode = REFLECTED_WEATHER_CODE
        assertEquals(weatherUriDoesNotMatch,
                expectedWeatherCode,
                actualWeatherCode)

        /*
         * Test that the code returned from our matcher matches the expected weather with date code
         */
        val weatherWithDateUriCodeDoesNotMatch = "Error: The CODE_WEATHER WITH DATE URI was matched incorrectly."
        val actualWeatherWithDateCode = testMatcher!!.match(TEST_WEATHER_WITH_DATE_DIR)
        val expectedWeatherWithDateCode = REFLECTED_WEATHER_WITH_DATE_CODE
        assertEquals(weatherWithDateUriCodeDoesNotMatch,
                expectedWeatherWithDateCode,
                actualWeatherWithDateCode)
    }

    companion object {

        private val TEST_WEATHER_DIR = WeatherContract.WeatherEntry.CONTENT_URI
        private val TEST_WEATHER_WITH_DATE_DIR = WeatherContract.WeatherEntry
                .buildWeatherUriWithDate(TestUtilities.DATE_NORMALIZED)

        private val weatherCodeVariableName = "CODE_WEATHER"
        private var REFLECTED_WEATHER_CODE: Int = 0

        private val weatherCodeWithDateVariableName = "CODE_WEATHER_WITH_DATE"
        private var REFLECTED_WEATHER_WITH_DATE_CODE: Int = 0
    }
}
