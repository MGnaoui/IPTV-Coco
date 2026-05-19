package com.iptvcoco.app.util

import android.view.View

/**
 * Helper for Android TV D-pad focus visuals.
 * Scales the view up slightly and adds elevation when focused.
 * No-op on phones/tablets (touch devices).
 */
object TvFocusHelper {

    fun apply(view: View, scale: Float = 1.06f, duration: Long = 150) {
        if (!DeviceUtils.isTv(view.context)) return

        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) scale else 1.0f)
                .scaleY(if (hasFocus) scale else 1.0f)
                .setDuration(duration)
                .start()
            v.elevation = if (hasFocus) 16f else 0f
        }
    }
}
