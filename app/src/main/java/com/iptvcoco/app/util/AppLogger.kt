package com.iptvcoco.app.util

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private lateinit var logFile: File
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val lock = Any()

    fun init(context: Context) {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        logFile = File(dir, "app_logs.txt")
    }

    fun logCrash(throwable: Throwable) {
        val timestamp = dateFormat.format(Date())
        val deviceInfo = buildString {
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("App: IPTV Coco")
        }

        val stackTrace = throwable.stackTraceToString()

        val log = buildString {
            appendLine("========== CRASH ==========")
            appendLine("Time: $timestamp")
            append(deviceInfo)
            appendLine("Exception: ${throwable.javaClass.name}: ${throwable.message}")
            appendLine(stackTrace)
            appendLine("===========================")
            appendLine()
        }

        writeToFile(log)
    }

    fun logEvent(message: String) {
        val timestamp = dateFormat.format(Date())
        val log = "[$timestamp] $message\n"
        writeToFile(log)
    }

    fun getLogFile(): File? = if (::logFile.isInitialized) logFile else null

    fun clearLogs() {
        synchronized(lock) {
            try {
                logFile.writeText("")
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    private fun writeToFile(text: String) {
        if (!::logFile.isInitialized) return
        synchronized(lock) {
            try {
                logFile.appendText(text)
            } catch (_: Exception) {
                // If we can't write logs, just silently fail
            }
        }
    }
}
