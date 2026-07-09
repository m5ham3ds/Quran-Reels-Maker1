package com.example.utils

import android.util.Log
import com.example.generator.SystemDiagnosticTracker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.ArrayDeque

object AppLogger {
    private const val MAX_LINES = 1000
    private val logs = ArrayDeque<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private fun appendLog(level: String, tag: String?, msg: String, tr: Throwable? = null) {
        val time = dateFormat.format(Date())
        val exceptionStr = if (tr != null) "\n" + Log.getStackTraceString(tr) else ""
        val logLine = "[$time] [$level] [$tag] $msg$exceptionStr"
        
        synchronized(logs) {
            if (logs.size >= MAX_LINES) {
                logs.poll()
            }
            logs.offer(logLine)
        }
        
        if (level == "E" || level == "F") {
            try {
                SystemDiagnosticTracker.addLog("ERROR", "$tag: $msg$exceptionStr", "ERROR")
            } catch(ignored: Exception) {}
        }
    }

    fun d(tag: String?, msg: String): Int {
        appendLog("D", tag, msg)
        return Log.d(tag, msg)
    }

    fun d(tag: String?, msg: String, tr: Throwable?): Int {
        appendLog("D", tag, msg, tr)
        return Log.d(tag, msg, tr)
    }

    fun e(tag: String?, msg: String): Int {
        appendLog("E", tag, msg)
        return Log.e(tag, msg)
    }

    fun e(tag: String?, msg: String, tr: Throwable?): Int {
        appendLog("E", tag, msg, tr)
        return Log.e(tag, msg, tr)
    }

    fun i(tag: String?, msg: String): Int {
        appendLog("I", tag, msg)
        return Log.i(tag, msg)
    }

    fun i(tag: String?, msg: String, tr: Throwable?): Int {
        appendLog("I", tag, msg, tr)
        return Log.i(tag, msg, tr)
    }

    fun w(tag: String?, msg: String): Int {
        appendLog("W", tag, msg)
        return Log.w(tag, msg)
    }

    fun w(tag: String?, msg: String, tr: Throwable?): Int {
        appendLog("W", tag, msg, tr)
        return Log.w(tag, msg, tr)
    }

    fun getStackTraceString(tr: Throwable?): String {
        return Log.getStackTraceString(tr)
    }

    fun getLogs(): String {
        synchronized(logs) {
            return logs.joinToString("\n")
        }
    }
}
