package fr.alexandreroman.wifiscanner.networks

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import fr.alexandreroman.wifiscanner.*

/**
 * [RecyclerView] adapter for [ScanResult] instances.
 * @author Alexandre Roman
 */
class ScanResultAdapter(
        private var items: MutableList<ScanResult> = ArrayList(8),
        var currentNetwork: String? = null)
    : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ssid: TextView = view.findViewById(R.id.networks_ssid)
        val bssid: TextView = view.findViewById(R.id.networks_bssid)
        val band: TextView = view.findViewById(R.id.networks_band)
        val channel: TextView = view.findViewById(R.id.networks_channel)
        val signalLevel: ImageView = view.findViewById(R.id.networks_signal_level)
        val locked: ImageView = view.findViewById(R.id.networks_locked)
    }

    fun update(scanResults: List<ScanResult>?) {
        items.clear()
        if (scanResults != null) {
            items.addAll(scanResults)
        }
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return items[position].SSID.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_network, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = items[position]
        holder.ssid.text = scanResult.SSID
        if (currentNetwork == scanResult.SSID) {
            holder.ssid.setTextColor(ContextCompat.getColor(holder.ssid.context, R.color.colorAccent))
        } else {
            holder.ssid.setTextColor(ContextCompat.getColor(holder.ssid.context, android.R.color.primary_text_light))
        }
        holder.signalLevel.setImageDrawable(holder.signalLevel.context.getDrawable(scanResult.toSignalLevelResource()))
        holder.bssid.text = scanResult.BSSID.toUpperCase()
        if (scanResult.is2Ghz()) {
            holder.band.setText(R.string.networks_band_24)
        } else if (scanResult.is5Ghz()) {
            holder.band.setText(R.string.networks_band_5)
        } else {
            holder.band.setText(R.string.networks_band_unknown)
        }
        holder.channel.text = holder.channel.context.getString(R.string.networks_channel).format(scanResult.getChannel())
        holder.locked.setImageDrawable(
                holder.locked.context.getDrawable(
                        if (scanResult.hasSecurityProtocol()) R.drawable.baseline_lock_24
                        else R.drawable.baseline_lock_open_24))
    }

    private fun ScanResult.toSignalLevelResource() =
            when (WifiManager.calculateSignalLevel(this.level, 5)) {
                0 -> R.drawable.baseline_signal_wifi_0_bar_24
                1 -> R.drawable.baseline_signal_wifi_1_bar_24
                2 -> R.drawable.baseline_signal_wifi_2_bar_24
                3 -> R.drawable.baseline_signal_wifi_3_bar_24
                4 -> R.drawable.baseline_signal_wifi_4_bar_24
                else -> throw IllegalStateException("Unexpected signal level")
            }
}
