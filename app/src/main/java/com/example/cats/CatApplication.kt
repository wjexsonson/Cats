package com.example.cats

import android.app.Application
import com.example.cats.di.AppComponent
import com.example.cats.di.DaggerAppComponent
import com.example.cats.di.DatabaseModule

class CatApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .databaseModule(DatabaseModule(this))
            .build()
    }
}
