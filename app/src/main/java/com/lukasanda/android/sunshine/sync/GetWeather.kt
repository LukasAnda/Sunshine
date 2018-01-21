package com.lukasanda.android.sunshine.sync

import com.lukasanda.android.sunshine.data.Forecast

/**
 * Created by lukas on 1/11/2018.
 */

class GetWeather(val apiService: ApiService) {
    fun getWeather(id:Int): io.reactivex.Observable<Forecast> {

        return apiService.getWeatherId(id = id.toString())

    }
}