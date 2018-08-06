package fr.alexandreroman.wifiscanner.hosts

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.net.InetAddress
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object NetworkDiscoverer {
    interface Handler {
        fun onHostFound(host: Host)
        fun onHostResolved(host: Host)
        fun onProgress(cur: Int, max: Int): Boolean
        fun onStart()
        fun onFinish()
    }

    fun start(handler: Handler) {
        val threadPool = ThreadPoolExecutor(4, 4,
                10, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())

        handler.onStart()

        val hostAddresses = mutableSetOf<InetAddress>()
        for (i in 1..10) {
            hostAddresses.add(InetAddress.getByName("192.168.1.$i"))
        }
        val max = hostAddresses.size

        val hosts = CopyOnWriteArraySet<Host>()

        for ((i, hostAddr) in hostAddresses.withIndex()) {
            threadPool.submit {
                val abort = handler.onProgress(i, max)
                if (HostPinger.pingHost(hostAddr)) {
                    val host = Host(hostAddr)

                    hosts.add(host)
                }
            }
        }
    }

    private fun pingHost(addr: InetAddress): Deferred<Host?> =
            async { if (HostPinger.pingHost(addr)) Host(addr) else null }

    private fun resolveHost(host: Host): Deferred<Host> =
            async { HostResolver.resolveHost(host); host }
}
