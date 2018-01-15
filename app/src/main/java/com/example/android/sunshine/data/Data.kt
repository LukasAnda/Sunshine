package com.example.android.sunshine.data

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Created by lukas on 1/11/2018.
 */
@RealmClass
open class Location(
    @PrimaryKey
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("city_name")
    var city_name: String? = null,
    @SerializedName("elevation")
    var elevation: Int = 0,
    @SerializedName("state_code")
    var state_code: String? = null,
    @SerializedName("state_name")
    var state_name: String? = null,
    @SerializedName("country_code")
    var country_code: String? = null,
    @SerializedName("country_name")
    var country_name: String? = null

):RealmObject()


@RealmClass
open class Forecast (
        @SerializedName("data")
    var data: RealmList<Data>? = null,
        @SerializedName("city_name")
    var city_name: String? = null,
        @SerializedName("lon")
    var lon: String? = null,
        @SerializedName("timezone")
    var timezone: String? = null,
        @SerializedName("lat")
    var lat: String? = null,
        @SerializedName("country_code")
    var country_code: String? = null,
        @SerializedName("state_code")
    var state_code: String? = null
):RealmObject()

@RealmClass
open class Weather (
    @SerializedName("icon")
    var icon: String? = null,
    @SerializedName("code")
    var code: String? = null,
    @SerializedName("description")
    var description: String? = null
):RealmObject()

@RealmClass
open class Data (
        @SerializedName("wind_cdir")
    var wind_cdir: String? = null,
        @SerializedName("rh")
    var rh: Double = 0.toDouble(),
        @SerializedName("wind_spd")
    var wind_spd: Double = 0.toDouble(),
        @SerializedName("pop")
    var pop: Double = 0.toDouble(),
        @SerializedName("wind_cdir_full")
    var wind_cdir_full: String? = null,
        @SerializedName("slp")
    var slp: Double = 0.toDouble(),
        @SerializedName("app_max_temp")
    var app_max_temp: Double = 0.toDouble(),
        @SerializedName("pres")
    var pres: Double = 0.toDouble(),
        @SerializedName("dewpt")
    var dewpt: Double = 0.toDouble(),
        @SerializedName("snow")
    var snow: Double = 0.toDouble(),
        @SerializedName("uv")
    var uv: Double = 0.toDouble(),
        @SerializedName("ts")
    var ts: Long = 0,
        @SerializedName("wind_dir")
    var wind_dir: Double = 0.toDouble(),
        @SerializedName("weather")
    var weather: Weather? = null,
        @SerializedName("app_min_temp")
    var app_min_temp: Double = 0.toDouble(),
        @SerializedName("max_temp")
    var max_temp: Double = 0.toDouble(),
        @SerializedName("snow_depth")
    var snow_depth: Double = 0.toDouble(),
        @SerializedName("precip")
    var precip: Double = 0.toDouble(),
        @SerializedName("max_dhi")
    var max_dhi: Double = 0.toDouble(),
        @SerializedName("datetime")
    @PrimaryKey
    var datetime: String? = null,
        @SerializedName("temp")
    var temp: Double = 0.toDouble(),
        @SerializedName("min_temp")
    var min_temp: Double = 0.toDouble(),
        @SerializedName("clouds")
    var clouds: Double = 0.toDouble(),
        @SerializedName("vis")
    var vis: Double = 0.toDouble()
):RealmObject()
