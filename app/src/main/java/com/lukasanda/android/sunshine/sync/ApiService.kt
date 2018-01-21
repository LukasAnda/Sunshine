package com.lukasanda.android.sunshine.sync

import com.lukasanda.android.sunshine.BuildConfig
import com.lukasanda.android.sunshine.data.Forecast

/**
 * Created by lukas on 1/11/2018.
 */
interface ApiService {

    @retrofit2.http.GET("forecast/daily")
    fun getWeatherId(@retrofit2.http.Query(CITY_ID) id: String,
                     @retrofit2.http.Query("key") days: String = BuildConfig.OPEN_WEATHER_MAP_API_KEY):
            io.reactivex.Observable<Forecast>
    companion object Factory {
        private val FORECAST_BASE_URL = "https://api.weatherbit.io/v2.0/"

        private const val CITY_ID = "city_id"

        fun create(): ApiService {
            val retrofit = retrofit2.Retrofit.Builder()
                    .addCallAdapterFactory(retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory.create())
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .baseUrl(FORECAST_BASE_URL)
                    .build()

            return retrofit.create(ApiService::class.java);
        }

    }
}