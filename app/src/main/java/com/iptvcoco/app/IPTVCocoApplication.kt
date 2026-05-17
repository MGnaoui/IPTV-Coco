package com.iptvcoco.app

import android.app.Application
import com.iptvcoco.app.repository.IPTVRepository

class IPTVCocoApplication : Application() {
    companion object {
        lateinit var instance: IPTVCocoApplication
            private set
    }

    val repository: IPTVRepository by lazy { IPTVRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
