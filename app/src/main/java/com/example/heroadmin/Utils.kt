package com.example.heroadmin

import android.util.Log

fun logLargeString(tag: String, content: String) {
    val maxLogSize = 1000
    for (i in 0..content.length / maxLogSize) {
        val start = i * maxLogSize
        var end = (i + 1) * maxLogSize
        end = if (end > content.length) content.length else end
        Log.i(tag, content.substring(start, end))
    }
}