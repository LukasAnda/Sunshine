package com.example.android.sunshine.sync

import com.example.android.sunshine.data.Forecast

/**
 * Created by lukas on 1/11/2018.
 */

class GetWeather(val apiService: ApiService) {
    fun getWeather(lat: Double, lon: Double): io.reactivex.Observable<Forecast> {

        return apiService.getWeatherLatLon(lat = lat.toString(), lon = lon.toString())

    }
}