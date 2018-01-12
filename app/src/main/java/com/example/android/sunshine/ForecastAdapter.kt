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

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.android.sunshine.data.Data
import com.example.android.sunshine.utilities.SunshineWeatherUtils
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import org.joda.time.format.DateTimeFormat

internal class ForecastAdapter(
        private val mContext: Context,
        private val mClickHandler: ForecastAdapterOnClickHandler,
        private var weatherData:OrderedRealmCollection<Data>) : RealmRecyclerViewAdapter<Data,ForecastAdapter.ForecastAdapterViewHolder>(weatherData,true) {
    private val mUseTodayLayout: Boolean


    interface ForecastAdapterOnClickHandler {
        fun onClick(date: String?)
    }


    init {
        mUseTodayLayout = mContext.resources.getBoolean(R.bool.use_today_layout)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ForecastAdapterViewHolder {

        val layoutId: Int

        when (viewType) {

            VIEW_TYPE_TODAY -> {
                layoutId = R.layout.list_item_forecast_today
            }

            VIEW_TYPE_FUTURE_DAY -> {
                layoutId = R.layout.forecast_list_item
            }

            else -> throw IllegalArgumentException("Invalid view type, value of " + viewType)
        }

        val view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false)

        view.isFocusable = true

        return ForecastAdapterViewHolder(view)
    }

    override fun onBindViewHolder(forecastAdapterViewHolder: ForecastAdapterViewHolder, position: Int) {

        /****************
         * Weather Icon *
         */
        val day = getItem(position)
        if(day==null)return
        val weatherId = day.weather?.code?.toInt()
        val weatherImageId: Int

        val viewType = getItemViewType(position)

        when (viewType) {

            VIEW_TYPE_TODAY -> weatherImageId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId)

            VIEW_TYPE_FUTURE_DAY -> weatherImageId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId)

            else -> throw IllegalArgumentException("Invalid view type, value of " + viewType)
        }

        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId)

        /****************
         * Weather Date *
         */
        /* Read date from the cursor */
        val date = day?.datetime
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val jodaDate = formatter.parseDateTime(date)
        val dateText = jodaDate.toString("EEEE, MMMMM dd")

        /* Display friendly date string */
        forecastAdapterViewHolder.dateView.text = dateText

        /***********************
         * Weather Description *
         */
        val description = day.weather?.description //SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId)
        /* Create the accessibility (a11y) String from the weather description */
        val descriptionA11y = mContext.getString(R.string.a11y_forecast, description)

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.descriptionView.text = description
        forecastAdapterViewHolder.descriptionView.contentDescription = descriptionA11y

        /**************************
         * High (max) temperature *
         */
        /* Read high temperature from the cursor (in degrees celsius) */
        val highInCelsius = day.max_temp
        /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        val highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius)
        /* Create the accessibility (a11y) String from the weather description */
        val highA11y = mContext.getString(R.string.a11y_high_temp, highString)

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.highTempView.text = highString
        forecastAdapterViewHolder.highTempView.contentDescription = highA11y

        /*************************
         * Low (min) temperature *
         */
        val lowInCelsius =day.min_temp
        /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        val lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius)
        val lowA11y = mContext.getString(R.string.a11y_low_temp, lowString)

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.lowTempView.text = lowString
        forecastAdapterViewHolder.lowTempView.contentDescription = lowA11y
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (today or future day)
     */
    override fun getItemViewType(position: Int): Int {
        return if (mUseTodayLayout && position == 0) {
            VIEW_TYPE_TODAY
        } else {
            VIEW_TYPE_FUTURE_DAY
        }
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    internal inner class ForecastAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val iconView: ImageView

        val dateView: TextView
        val descriptionView: TextView
        val highTempView: TextView
        val lowTempView: TextView

        init {

            iconView = view.findViewById<View>(R.id.weather_icon) as ImageView
            dateView = view.findViewById<View>(R.id.date) as TextView
            descriptionView = view.findViewById<View>(R.id.weather_description) as TextView
            highTempView = view.findViewById<View>(R.id.high_temperature) as TextView
            lowTempView = view.findViewById<View>(R.id.low_temperature) as TextView

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
            val adapterPosition = adapterPosition
            val dateInMillis = getItem(adapterPosition)?.datetime
            mClickHandler.onClick(dateInMillis)
        }
    }

    companion object {

        private val VIEW_TYPE_TODAY = 0
        private val VIEW_TYPE_FUTURE_DAY = 1
    }
}