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
        val macAddress: TextView = view.findViewById(R.id.info_mac_address)
        val ipAddress: TextView = view.findViewById(R.id.info_ip_address)
        val netmask: TextView = view.findViewById(R.id.info_netmask)
        val gatewayAddress: TextView = view.findViewById(R.id.info_gateway_address)
        val dnsServers: TextView = view.findViewById(R.id.info_dns_servers)
        val networkMetered: TextView = view.findViewById(R.id.info_network_metered)
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
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
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
            holder.macAddress.text = wifiInfo!!.macAddress ?: context.getString(R.string.info_value_na)
            holder.ipAddress.text = wifiInfo!!.ipAddress.format()
            holder.netmask.text = wifiInfo!!.netmask.format()
            holder.gatewayAddress.text = wifiInfo!!.gatewayAddress.format()

            if (wifiInfo!!.dnsServers.isEmpty()) {
                holder.dnsServers.text = context.getText(R.string.info_value_unknown)
            } else {
                holder.dnsServers.text = wifiInfo!!.dnsServers.map { it.format() }.joinToString(separator = "\n")
            }

            holder.networkMetered.text = wifiInfo!!.networkMetered.format(context)
        }
    }

    private fun Boolean.format(context: Context) =
            if (this) context.getString(R.string.info_value_yes) else context.getString(R.string.info_value_no)

    private fun InetAddress.format() = this.hostAddress
}
