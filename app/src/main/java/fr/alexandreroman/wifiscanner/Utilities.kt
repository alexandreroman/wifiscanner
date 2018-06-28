package fr.alexandreroman.wifiscanner

import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo

fun WifiInfo.getSsid() =
        if (isSsidUnknown() || ssid == null) {
            // This may happen if the user has not granted location permission.
            null
        } else if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid.substring(1, ssid.length - 1)
        } else {
            // SSID cannot be decoded as an UTF-8 string.
            null
        }

inline fun WifiInfo.isSsidUnknown() = "<unknown ssid>" == this.ssid

inline fun ScanResult.hasSecurityProtocol() =
        capabilities.contains("WPA") || capabilities.contains("WEP")
