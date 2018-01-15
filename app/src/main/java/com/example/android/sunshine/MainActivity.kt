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
package com.example.android.sunshine

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.android.sunshine.data.Data
import com.example.android.sunshine.data.Location
import com.example.android.sunshine.data.SunshinePreferences
import com.example.android.sunshine.sync.SunshineSyncUtils
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_forecast.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(),  ForecastAdapter.ForecastAdapterOnClickHandler {


    private val TAG = MainActivity::class.java.simpleName

    private var mForecastAdapter: ForecastAdapter? = null
    private var mPosition = RecyclerView.NO_POSITION
    private var realm: Realm by Delegates.notNull()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        supportActionBar?.elevation = 0f
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        recyclerView.setHasFixedSize(true)
        realm = Realm.getDefaultInstance()
        val d = realm.where(Data::class.java).findAll()
        mForecastAdapter = ForecastAdapter(this, this,d)

        recyclerView.adapter = mForecastAdapter
        syncData()
        Log.d(TAG,"Cities -> "+ realm.where(Location::class.java).findAll().size)
    }


    fun syncData(){
        SunshineSyncUtils.initialize(this)
    }

    override fun onResume() {
        super.onResume()
        mForecastAdapter?.notifyDataSetChanged()
    }


    /**
     * Uses the URI scheme for showing a location found on a map in conjunction with
     * an implicit Intent. This super-handy Intent is detailed in the "Common Intents" page of
     * Android's developer site:
     *
     * @see "http://developer.android.com/guide/components/intents-common.html.Maps"
     *
     *
     * Protip: Hold Command on Mac or Control on Windows and click that link to automagically
     * open the Common Intents page
     */
    private fun openPreferredLocationInMap() {
        val coords = SunshinePreferences.getLocationCoordinates(this)
        val posLat = java.lang.Double.toString(coords[0])
        val posLong = java.lang.Double.toString(coords[1])
        val geoLocation = Uri.parse("geo:$posLat,$posLong")

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = geoLocation

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!")
        }
    }

    override fun onClick(date: String?) {
        val weatherDetailIntent = Intent(this@MainActivity, DetailActivity::class.java)
        weatherDetailIntent.putExtra("date",date)
        startActivity(weatherDetailIntent)
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     *
     *
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private fun showWeatherDataView() {
        /* First, hide the loading indicator */
        progressBar.visibility = View.INVISIBLE
        /* Finally, make sure the weather data is visible */
        recyclerView.visibility = View.VISIBLE
    }

    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     *
     *
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private fun showLoading() {
        /* Then, hide the weather data */
        recyclerView.visibility = View.INVISIBLE
        /* Finally, show the loading indicator */
        progressBar.visibility = View.VISIBLE
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     *
     * @see .onPrepareOptionsMenu
     *
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu)
        /* Return true so that the menu is displayed in the Toolbar */
        return true
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
