package com.exercise.supercom.coronaapp.data

import android.net.ConnectivityManager
import androidx.annotation.Keep

@Keep
enum class ConnectionType(val value: Int, val connectionName: String) {
    WIFI(ConnectivityManager.TYPE_WIFI, "WIFI"),
    MOBILE(ConnectivityManager.TYPE_MOBILE, "Cellular"),
    OFFLINE(64 * (ConnectivityManager.TYPE_WIFI + ConnectivityManager.TYPE_MOBILE), "Offline"),
    UNKNOWN(ConnectivityManager.TYPE_DUMMY, "Unknown");

    companion object {
        fun fromValue(managerValue: Int): ConnectionType {
            return values().firstOrNull { it.value == managerValue } ?: UNKNOWN
        }
    }
}