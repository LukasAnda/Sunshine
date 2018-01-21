package com.lukasanda.android.sunshine

import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by lukas on 1/11/2018.
 */
class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().schemaVersion(2).build()
        Realm.setDefaultConfiguration(config)
    }
}