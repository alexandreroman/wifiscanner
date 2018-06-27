package fr.alexandreroman.wifiscanner.info

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.support.v4.content.ContextCompat
import timber.log.Timber
import java.net.InetAddress

/**
 * [WifiInfo] repository.
 * @author Alexandre Roman
 */
object WifiInfoRepository {
    fun load(context: Context): WifiInfo? {
        Timber.d("Loading Wi-Fi information")

        val wifiMan = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiMan.isWifiEnabled) {
            Timber.d("Wi-Fi is disabled")
            return null
        }
        val connMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connMan.activeNetworkInfo
        if (netInfo == null || netInfo.type != ConnectivityManager.TYPE_WIFI) {
            Timber.d("Active network is not using Wi-Fi")
            return null
        }

        if (!netInfo.isConnected) {
            Timber.d("Current Wi-Fi network is not connected")
            return null
        }

        val wifiConnInfo = wifiMan.connectionInfo
        val networkMetered = connMan.isActiveNetworkMetered

        val ssid: String?
        if ("<unknown ssid>".equals(wifiConnInfo.ssid) || wifiConnInfo.ssid == null) {
            // This may happen if the user has not granted location permission.
            ssid = null
        } else if (wifiConnInfo.ssid.startsWith("\"") && wifiConnInfo.ssid.endsWith("\"")) {
            ssid = wifiConnInfo.ssid.substring(1, wifiConnInfo.ssid.length - 1)
        } else {
            // SSID cannot be decoded as an UTF-8 string.
            ssid = null
        }

        val hiddenSsid = wifiConnInfo.hiddenSSID
        val signalLevel = WifiManager.calculateSignalLevel(wifiConnInfo.rssi, 10)
        val macAddress = if (!"02:00:00:00:00:00".equals(wifiConnInfo.macAddress)) wifiConnInfo.macAddress else null
        val ipAddress = wifiConnInfo.ipAddress.toInetAddress()
        val linkSpeed = wifiConnInfo.linkSpeed

        val dhcpInfo = wifiMan.dhcpInfo
        val gatewayAddress = dhcpInfo.gateway.toInetAddress()
        val netmask = dhcpInfo.netmask.toInetAddress()

        val dnsServers = mutableListOf<InetAddress>()
        if (dhcpInfo.dns1 != 0) {
            dnsServers.add(dhcpInfo.dns1.toInetAddress())
        }
        if (dhcpInfo.dns2 != 0) {
            dnsServers.add(dhcpInfo.dns2.toInetAddress())
        }

        val permissionsRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

        val result = WifiInfo(
                ssid = ssid,
                hiddenSsid = hiddenSsid,
                signalLevel = signalLevel,
                macAddress = macAddress,
                ipAddress = ipAddress,
                linkSpeed = linkSpeed,
                gatewayAddress = gatewayAddress,
                netmask = netmask,
                dnsServers = dnsServers,
                networkMetered = networkMetered,
                permissionsRequired = permissionsRequired
        )
        Timber.d("Wi-Fi information loaded: %s", result)
        return result
    }

    private fun Int.toInetAddress() =
            InetAddress.getByName(
                    String.format("%d.%d.%d.%d",
                            (this and 0xff),
                            (this shr 8 and 0xff),
                            (this shr 16 and 0xff),
                            (this shr 24 and 0xff)
                    ))
}
