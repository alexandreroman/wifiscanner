package fr.alexandreroman.wifiscanner.hosts

import timber.log.Timber
import java.net.InetAddress

/**
 * Service for resolving [Host] instances.
 * @author Alexandre Roman
 */
object HostResolver {
    /**
     * Resolve host name. This method is blocking as network requests are sent:
     * do not call this method on the UI thread.
     * @return true if host name was resolved
     */
    fun resolveHost(host: Host): Boolean {
        val addr = host.address.hostAddress
        Timber.d("Resolving host name: %s", addr)
        // Use a new InetAddress instance to "force" host name resolution.
        host.name = InetAddress.getByName(addr).canonicalHostName
        Timber.d("Host %s resolved to %s", addr, host.name)
        return host.name != null
    }
}
