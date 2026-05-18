package com.iptvcoco.app

import android.app.Application
import android.content.ComponentCallbacks2
import com.bumptech.glide.Glide
import com.iptvcoco.app.repository.IPTVRepository
import com.iptvcoco.app.util.AppLogger

class IPTVCocoApplication : Application() {
    companion object {
        lateinit var instance: IPTVCocoApplication
            private set
    }

    val repository: IPTVRepository by lazy { IPTVRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppLogger.init(this)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.logCrash(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Clear Glide memory cache when system is low on memory
                Glide.get(this).clearMemory()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }
}
