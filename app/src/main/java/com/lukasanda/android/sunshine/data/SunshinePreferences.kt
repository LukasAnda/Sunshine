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
package com.lukasanda.android.sunshine.data

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.app.FragmentActivity
import com.lukasanda.android.sunshine.R

object SunshinePreferences {

    /*
     * In order to uniquely pinpoint the location on the map when we launch the map intent, we
     * store the latitude and longitude. We will also use the latitude and longitude to create
     * queries for the weather.
     */
    val PREF_COORD_LAT = "coord_lat"
    val PREF_COORD_LONG = "coord_long"

    /**
     * Helper method to handle setting location details in Preferences (city name, latitude,
     * longitude)
     *
     *
     * When the location details are updated, the database should to be cleared.
     *
     * @param context  Context used to get the SharedPreferences
     * @param lat      the latitude of the city
     * @param lon      the longitude of the city
     */
    fun setLocationDetails(context: Context, lat: Double, lon: Double) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()

        editor.putLong(PREF_COORD_LAT, java.lang.Double.doubleToRawLongBits(lat))
        editor.putLong(PREF_COORD_LONG, java.lang.Double.doubleToRawLongBits(lon))
        editor.apply()
    }

    /**
     * Resets the location coordinates stores in SharedPreferences.
     *
     * @param context Context used to get the SharedPreferences
     */

    fun setPrefferedWeatherLocation(context: Context, id:Int){
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val ed = sp.edit()

        val keyForLocation = context.getString(R.string.pref_location_key)
        ed.putInt(keyForLocation, id)
        ed.apply()
    }

    fun getPreferredWeatherLocation(context: Context): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        val keyForLocation = context.getString(R.string.pref_location_key)
        val defaultLocation = context.getString(R.string.pref_location_default).toInt()

        return sp.getInt(keyForLocation, defaultLocation)
    }

    /**
     * Returns true if the user has selected metric temperature display.
     *
     * @param context Context used to get the SharedPreferences
     * @return true if metric display should be used, false if imperial display should be used
     */
    fun isMetric(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        val keyForUnits = context.getString(R.string.pref_units_key)
        val defaultUnits = context.getString(R.string.pref_units_metric)
        val preferredUnits = sp.getString(keyForUnits, defaultUnits)
        val metric = context.getString(R.string.pref_units_metric)

        var userPrefersMetric = false
        if (metric == preferredUnits) {
            userPrefersMetric = true
        }

        return userPrefersMetric
    }

    /**
     * Returns the location coordinates associated with the location. Note that there is a
     * possibility that these coordinates may not be set, which results in (0,0) being returned.
     * Interestingly, (0,0) is in the middle of the ocean off the west coast of Africa.
     *
     * @param context used to access SharedPreferences
     * @return an array containing the two coordinate values for the user's preferred location
     */
    fun getLocationCoordinates(context: Context): DoubleArray {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        val preferredCoordinates = DoubleArray(2)

        /*
         * This is a hack we have to resort to since you can't store doubles in SharedPreferences.
         *
         * Double.doubleToLongBits returns an integer corresponding to the bits of the given
         * IEEE 754 double precision value.
         *
         * Double.longBitsToDouble does the opposite, converting a long (that represents a double)
         * into the double itself.
         */
        preferredCoordinates[0] = java.lang.Double
                .longBitsToDouble(sp.getLong(PREF_COORD_LAT, java.lang.Double.doubleToRawLongBits(0.0)))
        preferredCoordinates[1] = java.lang.Double
                .longBitsToDouble(sp.getLong(PREF_COORD_LONG, java.lang.Double.doubleToRawLongBits(0.0)))

        return preferredCoordinates
    }

    /**
     * Returns true if the latitude and longitude values are available. The latitude and
     * longitude will not be available until the lesson where the PlacePicker API is taught.
     *
     * @param context used to get the SharedPreferences
     * @return true if lat/long are saved in SharedPreferences
     */
    fun isLocationLatLonAvailable(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        val spContainLatitude = sp.contains(PREF_COORD_LAT)
        val spContainLongitude = sp.contains(PREF_COORD_LONG)

        var spContainBothLatitudeAndLongitude = false
        if (spContainLatitude && spContainLongitude) {
            spContainBothLatitudeAndLongitude = true
        }

        return spContainBothLatitudeAndLongitude
    }

    /**
     * Returns true if the user prefers to see notifications from Sunshine, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to see notifications, false otherwise
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        /* Key for accessing the preference for showing notifications */
        val displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key)

        /*
         * In Sunshine, the user has the ability to say whether she would like notifications
         * enabled or not. If no preference has been chosen, we want to be able to determine
         * whether or not to show them. To do this, we reference a bool stored in bools.xml.
         */
        val shouldDisplayNotificationsByDefault = context
                .resources
                .getBoolean(R.bool.show_notifications_by_default)

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp
                .getBoolean(displayNotificationsKey, shouldDisplayNotificationsByDefault)
    }

    /**
     * Returns the last time that a notification was shown (in UNIX time)
     *
     * @param context Used to access SharedPreferences
     * @return UNIX time of when the last notification was shown
     */
    fun getLastNotificationTimeInMillis(context: Context): Long {
        /* Key for accessing the time at which Sunshine last displayed a notification */
        val lastNotificationKey = context.getString(R.string.pref_last_notification)

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        /*
         * Here, we retrieve the time in milliseconds when the last notification was shown. If
         * SharedPreferences doesn't have a value for lastNotificationKey, we return 0. The reason
         * we return 0 is because we compare the value returned from this method to the current
         * system time. If the difference between the last notification time and the current time
         * is greater than one day, we will show a notification again. When we compare the two
         * values, we subtract the last notification time from the current system time. If the
         * time of the last notification was 0, the difference will always be greater than the
         * number of milliseconds in a day and we will show another notification.
         */

        return sp.getLong(lastNotificationKey, 0)
    }

    /**
     * Returns the elapsed time in milliseconds since the last notification was shown. This is used
     * as part of our check to see if we should show another notification when the weather is
     * updated.
     *
     * @param context Used to access SharedPreferences as well as use other utility methods
     * @return Elapsed time in milliseconds since the last notification was shown
     */
    fun getEllapsedTimeSinceLastNotification(context: Context): Long {
        val lastNotificationTimeMillis = SunshinePreferences.getLastNotificationTimeInMillis(context)
        return System.currentTimeMillis() - lastNotificationTimeMillis
    }

    /**
     * Saves the time that a notification is shown. This will be used to get the ellapsed time
     * since a notification was shown.
     *
     * @param context Used to access SharedPreferences
     * @param timeOfNotification Time of last notification to save (in UNIX time)
     */
    fun saveLastNotificationTime(context: Context, timeOfNotification: Long) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        val lastNotificationKey = context.getString(R.string.pref_last_notification)
        editor.putLong(lastNotificationKey, timeOfNotification)
        editor.apply()
    }
}