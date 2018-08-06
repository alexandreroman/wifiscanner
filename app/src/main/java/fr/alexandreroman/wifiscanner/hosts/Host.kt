package fr.alexandreroman.wifiscanner.hosts

import java.net.InetAddress

/**
 * Host scanned on a network.
 * @author Alexandre Roman
 */
data class Host(
        val address: InetAddress,
        var name: String? = null
)
