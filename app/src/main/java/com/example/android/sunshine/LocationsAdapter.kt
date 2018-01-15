package com.example.android.sunshine

/**
 * Created by lukas on 1/14/2018.
 */
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.example.android.sunshine.data.Location
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter

internal class LocationsAdapter(
        private val mContext: Context,
        private val mClickHandler: LocationsAdapterOnClickHandler,
        private var locations:OrderedRealmCollection<Location>) : RealmRecyclerViewAdapter<Location,LocationsAdapter.LocationsAdapterViewHolder>(locations,true), Filterable {

    override fun getFilter(): Filter {
        return LocationsFilter(this)
    }

    private val mUseTodayLayout: Boolean


    interface LocationsAdapterOnClickHandler {
        fun onClick(date: String?)
    }


    init {
        mUseTodayLayout = mContext.resources.getBoolean(R.bool.use_today_layout)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): LocationsAdapterViewHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.list_item_location, viewGroup, false)

        view.isFocusable = true

        return LocationsAdapterViewHolder(view)
    }

    override fun onBindViewHolder(locationsAdapterViewHolder: LocationsAdapterViewHolder, position: Int) {

        val location = getItem(position)
        locationsAdapterViewHolder.cityName.text = "${location?.city_name} - ${location?.country_name}"
    }
    override fun getItemViewType(position: Int): Int {
        return if (mUseTodayLayout && position == 0) {
            VIEW_TYPE_TODAY
        } else {
            VIEW_TYPE_FUTURE_DAY
        }
    }


    fun filterResults(text:String) {
        val text = text.toLowerCase().trim()
        val query = Realm.getDefaultInstance().where(Location::class.java);
        if(!("".equals(text)) && text.length>3) {
            query.contains("city_name", text, Case.INSENSITIVE).or().contains("country_name", text, Case.INSENSITIVE)
        }
        updateData(query.findAll())
    }
    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    internal inner class LocationsAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val cityName: TextView

        init {

            cityName = view.findViewById<View>(R.id.cityName) as TextView

            view.setOnClickListener(this)
        }
        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        override fun onClick(v: View) {
//            val adapterPosition = adapterPosition
//            val dateInMillis = getItem(adapterPosition)?.datetime
            mClickHandler.onClick(null)
        }
    }

    private inner class LocationsFilter constructor(private val adapter: LocationsAdapter) : Filter() {

        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            return Filter.FilterResults()
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            adapter.filterResults(constraint.toString())
        }
    }

    companion object {

        private val VIEW_TYPE_TODAY = 0
        private val VIEW_TYPE_FUTURE_DAY = 1
    }
}