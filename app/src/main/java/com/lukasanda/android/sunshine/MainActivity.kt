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
package com.lukasanda.android.sunshine

import android.content.Intent
import android.databinding.adapters.ToolbarBindingAdapter
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lukasanda.android.sunshine.data.Data
import com.lukasanda.android.sunshine.data.SunshinePreferences
import com.lukasanda.android.sunshine.sync.SunshineSyncUtils
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_forecast.*
import kotlin.properties.Delegates
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.lukasanda.android.sunshine.data.Location


class MainActivity : AppCompatActivity(), ForecastAdapter.ForecastAdapterOnClickHandler {


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
        mForecastAdapter = ForecastAdapter(this, this, d)

        recyclerView.adapter = mForecastAdapter
        syncData()
        /* we add cities from our firebase database*/
        if (realm.where(Location::class.java).count() == 0L) {
            val database = FirebaseDatabase.getInstance()
            val ref = database.reference
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    realm.executeTransactionAsync(
                            Realm.Transaction { realm ->
                                dataSnapshot.children
                                        .map { it.getValue(Location::class.java) }
                                        .forEach { it?.let { it1 -> realm.insertOrUpdate(it1) } }
                            }, Realm.Transaction.OnSuccess {
                        Toast.makeText(this@MainActivity, "" + realm.where(Location::class.java).count(), Toast.LENGTH_SHORT).show()
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                }
            })
        }
    }


    private fun syncData() {
        SunshineSyncUtils.initialize(this)
    }

    override fun onResume() {
        super.onResume()
        mForecastAdapter?.notifyDataSetChanged()
    }

    override fun onClick(date: String?) {
        val weatherDetailIntent = Intent(this@MainActivity, DetailActivity::class.java)
        weatherDetailIntent.putExtra("date", date)
        startActivity(weatherDetailIntent)
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
            startActivity(Intent(this, LocationSelectActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}

