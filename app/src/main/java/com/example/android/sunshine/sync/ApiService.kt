package com.example.android.sunshine.sync

import com.example.android.sunshine.data.Forecast

/**
 * Created by lukas on 1/11/2018.
 */
interface ApiService {

    @retrofit2.http.GET("forecast/daily")
    fun getWeatherLatLon(@retrofit2.http.Query(LAT_PARAM) lat: String,
                         @retrofit2.http.Query(LON_PARAM) lon: String,
                         @retrofit2.http.Query("key") days: String = "31fc63cb95f845f399af85ecb3dcdf91"):
            io.reactivex.Observable<Forecast>
    companion object Factory {
        private val FORECAST_BASE_URL = "https://api.weatherbit.io/v2.0/"

        private const val LAT_PARAM = "lat"
        private const val LON_PARAM = "lon"

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