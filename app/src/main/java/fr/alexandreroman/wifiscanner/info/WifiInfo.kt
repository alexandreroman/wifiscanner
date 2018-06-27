package fr.alexandreroman.wifiscanner.info

import java.net.InetAddress

/**
 * Data class containing information about a Wi-Fi network.
 * @author Alexandre Roman
 */
data class WifiInfo(
        val ssid: String?,
        val hiddenSsid: Boolean,
        val signalLevel: Int,
        val macAddress: String?,
        val ipAddress: InetAddress,
        val netmask: InetAddress,
        val gatewayAddress: InetAddress,
        val dnsServers: List<InetAddress>,
        val linkSpeed: Int,
        val networkMetered: Boolean,
        val permissionsRequired: Boolean
)
