package com.iptvcoco.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

object DeviceUtils {

    fun isTv(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        val isTelevisionUi = (uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
        val hasLeanback = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        return isTelevisionUi || hasLeanback
    }

    /** Smaller cache on phones to save RAM; larger on TV for smooth D-pad navigation */
    fun getRecyclerViewCacheSize(context: Context): Int = if (isTv(context)) 20 else 8
}
