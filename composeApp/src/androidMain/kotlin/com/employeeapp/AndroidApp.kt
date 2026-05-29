package com.employeeapp

import android.app.Application
import com.employeeapp.di.androidModule
import com.employeeapp.di.commonModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AndroidApp : Application() {

    companion object {
        lateinit var instance: AndroidApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@AndroidApp)
            modules(commonModules + androidModule)
        }
    }
}
