package com.example.android.sunshine

import io.realm.Realm

/**
 * Created by lukas on 1/11/2018.
 */
class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}