package com.example.android.sunshine.sync

/**
 * Created by lukas on 1/11/2018.
 */
object GetWeatherProvider {
    fun provideWeatherProvider(): GetWeather {
        return GetWeather(ApiService.create())
    }
}