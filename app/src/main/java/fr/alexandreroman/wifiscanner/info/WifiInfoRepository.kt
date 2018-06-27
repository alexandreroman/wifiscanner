package fr.alexandreroman.wifiscanner.info

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.support.v4.content.ContextCompat
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress

/**
 * [WifiInfo] repository.
 * @author Alexandre Roman
 */
object WifiInfoRepository {
    private const val UNKNOWN_SSID = "<unknown ssid>"

    fun load(context: Context): WifiInfo? {
        Timber.d("Loading Wi-Fi information")

        val wifiMan = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiMan.isWifiEnabled) {
            Timber.d("Wi-Fi is disabled")
            return null
        }

        val connMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiNet: Network? = null

        // Find out a network providing a connection using Wi-Fi.
        connMan.allNetworks.forEach {
            val caps = connMan.getNetworkCapabilities(it)
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val netInfo = connMan.getNetworkInfo(it)
                if (netInfo != null && netInfo.isConnected) {
                    wifiNet = it
                }
            }
        }
        if (wifiNet == null) {
            Timber.d("No Wi-Fi network found")
            return null
        }

        val networkMetered: Boolean?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ConnectivityManager.activeNetwork is only available on API 23+.
            networkMetered = wifiNet!!.equals(connMan.activeNetwork) && connMan.isActiveNetworkMetered
        } else {
            // I'm sorry boy, I can't help you with this one.
            networkMetered = null
        }

        val wifiConnInfo = wifiMan.connectionInfo

        val ssid: String?
        if (UNKNOWN_SSID.equals(wifiConnInfo.ssid) || wifiConnInfo.ssid == null) {
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
        val linkSpeed = wifiConnInfo.linkSpeed

        val linkProps = connMan.getLinkProperties(wifiNet)
        val defaultRoute = linkProps.routes.findLast { it.isDefaultRoute }
        if (defaultRoute == null) {
            Timber.d("Current Wi-Fi connection is not ready")
            return null
        }

        val ipAddresses = linkProps.linkAddresses.map { it.address }
        val gatewayAddress = defaultRoute.gateway
        val dnsServers = linkProps.dnsServers

        val netmask: InetAddress = getNetmask(linkProps.linkAddresses.filter { it.address is Inet4Address }.first())

        val permissionsRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && UNKNOWN_SSID == wifiConnInfo.ssid

        val result = WifiInfo(
                ssid = ssid,
                hiddenSsid = hiddenSsid,
                signalLevel = signalLevel,
                ipAddresses = ipAddresses,
                netmask = netmask,
                linkSpeed = linkSpeed,
                gatewayAddress = gatewayAddress,
                dnsServers = dnsServers,
                networkMetered = networkMetered,
                permissionsRequired = permissionsRequired
        )
        Timber.d("Wi-Fi information loaded: %s", result)
        return result
    }

    private fun getNetmask(la: LinkAddress): InetAddress {
        val prefixLength = la.prefixLength
        return Integer.reverseBytes((0xffffffff shl (32 - prefixLength)).toInt()).toInetAddress()
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
