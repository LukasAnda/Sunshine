/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.sunshine

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.android.sunshine.data.Data
import com.example.android.sunshine.utilities.SunshineWeatherUtils
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.extra_weather_details.view.*
import kotlinx.android.synthetic.main.primary_weather_info.view.*
import org.joda.time.format.DateTimeFormat
import kotlin.properties.Delegates


class DetailActivity : AppCompatActivity() {

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private var mForecastSummary: String? = null

    /* The URI that is used to access the chosen day's weather details */
    private var mDate: String? = null
    private var realm: Realm by Delegates.notNull()
    private var day:Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_detail)

        mDate = intent.getStringExtra("date")
        realm = Realm.getDefaultInstance()
        day = realm.where(Data::class.java).equalTo("datetime",mDate).findFirst()
        day?.let {
            bindData()
        }
        /* This connects our Activity into the loader lifecycle. */
    }

    private fun bindData(){
        val weatherId = day?.weather?.code?.toInt()
        /* Use our utility method to determine the resource ID for the proper art */
        val weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)

        /* Set the resource ID on the icon to display the art */
        primaryInfo.weatherIcon.setImageResource(weatherImageId)

        /****************
         * Weather Date *
         ****************/
        /*
         * Read the date from the cursor. It is important to note that the date from the cursor
         * is the same date from the weather SQL table. The date that is stored is a GMT
         * representation at midnight of the date when the weather information was loaded for.
         *
         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
         * the date representation for the local date in local time.
         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
         */
        val date = day?.datetime
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val jodaDate = formatter.parseDateTime(date)
        val dateText = jodaDate.toString("EEEE, MMMMM dd")

        primaryInfo.date.text = dateText

        /***********************
         * Weather Description *
         ***********************/
        /* Use the weatherId to obtain the proper description */
        val description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId)

        /* Create the accessibility (a11y) String from the weather description */
        val descriptionA11y = getString(R.string.a11y_forecast, description)

        /* Set the text and content description (for accessibility purposes) */
        primaryInfo.weatherDescription.text = description
        primaryInfo.weatherDescription.contentDescription = descriptionA11y

        /* Set the content description on the weather image (for accessibility purposes) */
        primaryInfo.weatherIcon.contentDescription = descriptionA11y

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        val highInCelsius = day?.max_temp
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        val highString = highInCelsius?.let { SunshineWeatherUtils.formatTemperature(this, it) }

        /* Create the accessibility (a11y) String from the weather description */
        val highA11y = getString(R.string.a11y_high_temp, highString)

        /* Set the text and content description (for accessibility purposes) */
        primaryInfo.highTemperature.text = highString
        primaryInfo.highTemperature.contentDescription = highA11y

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        val lowInCelsius = day?.min_temp
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        val lowString = lowInCelsius?.let { SunshineWeatherUtils.formatTemperature(this, it) }

        val lowA11y = getString(R.string.a11y_low_temp, lowString)

        /* Set the text and content description (for accessibility purposes) */
        primaryInfo.lowTemperature.text = lowString
        primaryInfo.lowTemperature.contentDescription = lowA11y

        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        val humidity = day?.rh
        val humidityString = getString(R.string.format_humidity, humidity)

        val humidityA11y = getString(R.string.a11y_humidity, humidityString)

        /* Set the text and content description (for accessibility purposes) */
        extraDetails.humidity.text = humidityString
        extraDetails.humidity.contentDescription = humidityA11y

        extraDetails.humidityLabel.contentDescription = humidityA11y

        /****************************
         * Wind speed and direction *
         ****************************/
        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        val windSpeed = day?.wind_spd
        val windDirection = day?.wind_dir
        val windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection)

        val windA11y = getString(R.string.a11y_wind, windString)

        /* Set the text and content description (for accessibility purposes) */
        extraDetails.windMeasurement.text = windString
        extraDetails.windMeasurement.contentDescription = windA11y

        extraDetails.windLabel.contentDescription = windA11y

        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        val pressure = day?.pres

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        val pressureString = getString(R.string.format_pressure, pressure)

        val pressureA11y = getString(R.string.a11y_pressure, pressureString)

        /* Set the text and content description (for accessibility purposes) */
        extraDetails.pressure.text = pressureString
        extraDetails.pressure.contentDescription = pressureA11y

        extraDetails.pressureLabel.contentDescription = pressureA11y

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString)
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     *
     * @see android.app.Activity.onPrepareOptionsMenu
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's menuInflater property to get a handle on the menu inflater */
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        menuInflater.inflate(R.menu.detail, menu)
        /* Return true so that the menu is displayed in the Toolbar */
        return true
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* Get the ID of the clicked item */
        val id = item.itemId

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            val shareIntent = createShareForecastIntent()
            startActivity(shareIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private fun createShareForecastIntent(): Intent {
        val shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary!! + FORECAST_SHARE_HASHTAG)
                .intent
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        return shareIntent
    }


    companion object {

        /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
        private val FORECAST_SHARE_HASHTAG = " #SunshineApp"

        /*
     * The columns of data that we are interested in displaying within our DetailActivity's
     * weather display.
     */
    }
}