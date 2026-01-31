package com.alius.gmrstockplus.core

import android.app.Application
import android.content.Context

object AppContextProvider {
    lateinit var appContext: Context
        private set

    fun init(application: Application) {
        appContext = application.applicationContext
        println("📌 [AppContextProvider] Context inicializado")
    }
}