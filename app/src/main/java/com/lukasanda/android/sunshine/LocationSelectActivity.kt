package com.lukasanda.android.sunshine

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import com.lukasanda.android.sunshine.data.Location
import com.lukasanda.android.sunshine.data.SunshinePreferences
import com.lukasanda.android.sunshine.sync.SunshineSyncUtils
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_location_select.*

/**
 * Created by lukas on 1/14/2018.
 */

class LocationSelectActivity : AppCompatActivity(), LocationsAdapter.LocationsAdapterOnClickHandler {
    private lateinit var adapter: LocationsAdapter

    override fun onClick(city: Int?) {
        city?.let { SunshinePreferences.setPrefferedWeatherLocation(this, it) }
        Log.d("Number", city.toString())
        SunshineSyncUtils.startImmediateSync(this)
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_select)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        adapter = LocationsAdapter(this,this, getData())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val selectedId = SunshinePreferences.getPreferredWeatherLocation(this)
        val city = Realm.getDefaultInstance()
                .where(Location::class.java)
                .equalTo("id",selectedId)
                .findFirst()
                ?.city_name
        selected.text = "Selected city: $city"
    }

    private fun getData(): OrderedRealmCollection<Location> {
        return Realm.getDefaultInstance().where(Location::class.java).equalTo("city_name", "", Case.INSENSITIVE).findAll()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.location, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "City name"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                adapter.filterResults(query)
                if(adapter.itemCount>0){

                }
                //Task HERE
                return false
            }

        })

        /* Return true so that the menu is displayed in the Toolbar */
        return true
    }
}