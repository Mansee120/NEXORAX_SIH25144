package com.health.stridox

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.health.stridox.di.dbModule
import com.health.stridox.di.preferencesModule
import com.health.stridox.di.repoModule
import com.health.stridox.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application(), DefaultLifecycleObserver {

    companion object {
        @Volatile
        var isInForeground: Boolean = false
    }

    override fun onCreate() {
        super<Application>.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(preferencesModule, dbModule, repoModule, viewModelModule)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isInForeground = false
    }

}