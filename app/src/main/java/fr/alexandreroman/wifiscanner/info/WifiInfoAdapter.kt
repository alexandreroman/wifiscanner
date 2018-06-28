package fr.alexandreroman.wifiscanner.info

import android.Manifest
import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import fr.alexandreroman.wifiscanner.R
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * [RecyclerView.Adapter] implementation handling a [WifiInfo] instance.
 * @author Alexandre Roman
 */
class WifiInfoAdapter(val activity: Activity) : RecyclerView.Adapter<WifiInfoAdapter.ViewHolder>() {
    companion object {
        private const val DATA_TYPE = 0
        private const val PERM_TYPE = 1

        const val PERMISSION_REQUEST_CODE = 42
    }

    private var wifiInfo: WifiInfo? = null

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DataViewHolder(view: View) : ViewHolder(view) {
        val ssid: TextView = view.findViewById(R.id.info_ssid)
        val hiddenSsid: TextView = view.findViewById(R.id.info_hidden_ssid)
        val signalLevel: TextView = view.findViewById(R.id.info_signal_level)
        val ipAddress: TextView = view.findViewById(R.id.info_ip_address)
        val netmask: TextView = view.findViewById(R.id.info_netmask)
        val ipv6Addresses: TextView = view.findViewById(R.id.info_ipv6_addresses)
        val gatewayAddress: TextView = view.findViewById(R.id.info_gateway_address)
        val dnsServers: TextView = view.findViewById(R.id.info_dns_servers)
        val networkMetered: TextView = view.findViewById(R.id.info_network_metered)
        val linkSpeed: TextView = view.findViewById(R.id.info_link_speed)
        val frequency: TextView = view.findViewById(R.id.info_frequency)
        val domains: TextView = view.findViewById(R.id.info_domains)
        val httpProxy: TextView = view.findViewById(R.id.info_http_proxy)
        val ipv6Block: View = view.findViewById(R.id.info_ipv6_block)
    }

    fun update(newWifiInfo: WifiInfo?) {
        if (wifiInfo == null && newWifiInfo == null) {
            return
        }

        val oldWifiInfo = wifiInfo
        wifiInfo = newWifiInfo

        if (oldWifiInfo != null && newWifiInfo == null) {
            notifyItemRemoved(0)
        } else if (oldWifiInfo == null && newWifiInfo != null) {
            notifyItemInserted(0)
        } else if (oldWifiInfo != null && newWifiInfo != null) {
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int) =
            when (position) {
                0 -> DATA_TYPE
                1 -> PERM_TYPE
                else -> throw IllegalArgumentException("Invalid position: " + position)
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                DATA_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.card_info, parent, false)
                    DataViewHolder(view)
                }
                PERM_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.card_info_permissions, parent, false)

                    val permText = view.findViewById<TextView>(R.id.perm_warning)
                    permText.movementMethod = LinkMovementMethod.getInstance()

                    val reviewPermsButton = view.findViewById<Button>(R.id.review_permissions)
                    reviewPermsButton.setOnClickListener {
                        ActivityCompat.requestPermissions(activity,
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
                    }

                    ViewHolder(view)
                }
                else -> throw IllegalArgumentException("Invalid view type: " + viewType)
            }

    override fun getItemCount() = if (wifiInfo == null) 0 else if (wifiInfo!!.permissionsRequired) 2 else 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            val context = holder.ssid.context
            holder.ssid.text = wifiInfo!!.ssid ?: context.getString(R.string.info_value_na)
            holder.hiddenSsid.text = wifiInfo!!.hiddenSsid.format(context)
            holder.signalLevel.text = "%d/10".format(wifiInfo!!.signalLevel)
            holder.ipAddress.text = wifiInfo!!.ipAddresses.first { it is Inet4Address }.format()
            holder.netmask.text = wifiInfo!!.netmask.format()
            holder.ipv6Addresses.text = wifiInfo!!.ipAddresses
                    .filter { it is Inet6Address }
                    .sortedWith(InetAddressComparator).joinToString(separator = "\n") { it.format() }
            holder.ipv6Block.visibility = if (holder.ipv6Addresses.text.isNullOrBlank()) View.GONE else View.VISIBLE
            holder.gatewayAddress.text = wifiInfo!!.gatewayAddress.format()

            holder.dnsServers.text =
                    if (wifiInfo!!.dnsServers.isEmpty()) context.getText(R.string.info_value_unknown)
                    else wifiInfo!!.dnsServers.sortedWith(InetAddressComparator)
                            .map { it.format() }
                            .joinToString(separator = "\n")

            holder.networkMetered.text = wifiInfo!!.networkMetered?.format(context) ?: context.getString(R.string.info_value_unknown)

            holder.linkSpeed.text = "%d %s".format(wifiInfo!!.linkSpeed, android.net.wifi.WifiInfo.LINK_SPEED_UNITS)
            holder.frequency.text = "%d %s".format(wifiInfo!!.frequency, android.net.wifi.WifiInfo.FREQUENCY_UNITS)
            holder.domains.text =
                    if (wifiInfo!!.domains.isEmpty()) context.getString(R.string.info_value_none)
                    else wifiInfo!!.domains.sorted().joinToString(separator = "\n")
            holder.httpProxy.text =
                    if (wifiInfo!!.httpProxy == null || wifiInfo!!.httpProxy!!.host == null) context.getString(R.string.info_value_none)
                    else "%s:%d".format(wifiInfo!!.httpProxy!!.host, wifiInfo!!.httpProxy!!.port)
        }
    }

    private fun Boolean.format(context: Context) =
            if (this) context.getString(R.string.info_value_yes) else context.getString(R.string.info_value_no)

    private fun InetAddress.format() = this.hostAddress

    object InetAddressComparator : Comparator<InetAddress> {
        override fun compare(o1: InetAddress?, o2: InetAddress?): Int {
            if (o1 is Inet4Address && o2 is Inet4Address ||
                    o1 is Inet6Address && o2 is Inet6Address) {
                return o1.hostAddress.compareTo(o2.hostAddress)
            }
            if (o1 is Inet4Address && o2 is Inet6Address) {
                return -1
            }
            return 1
        }
    }
}
