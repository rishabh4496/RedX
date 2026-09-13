package com.example.redx.util

import android.util.Log

/** Small structured logger that deliberately accepts fields instead of raw secrets or URLs. */
object RedXLogger {

    private const val TAG = "RedX"

    fun warning(event: String, vararg fields: Pair<String, Any?>) {
        val details = fields
            .filter { it.second != null }
            .joinToString(separator = " ") { (key, value) -> "$key=$value" }
        Log.w(TAG, if (details.isBlank()) event else "$event $details")
    }
}
