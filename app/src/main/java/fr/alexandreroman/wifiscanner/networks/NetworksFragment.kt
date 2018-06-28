/*
 * Copyright 2018 Alexandre Roman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.wifiscanner.networks

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.afollestad.materialdialogs.MaterialDialog
import fr.alexandreroman.wifiscanner.R
import fr.alexandreroman.wifiscanner.getSsid
import fr.alexandreroman.wifiscanner.nav.NavFragment
import timber.log.Timber

/**
 * Fragment displaying the "networks" tab.
 * @author Alexandre Roman
 */
class NetworksFragment : NavFragment() {
    companion object {
        const val PERMISSION_REQUEST_CODE = 28

        @JvmStatic
        fun newInstance(): NetworksFragment {
            return NetworksFragment()
        }
    }

    private val listAdapter = ScanResultAdapter()

    private val scanResultHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val wifiMan = context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val scanResults = wifiMan.scanResults
            NetworksViewModel.from(this@NetworksFragment).update(scanResults)
        }
    }

    override fun refresh() {
        Timber.d("Refreshing tab: networks")
        onStartScan()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_networks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startScan = view.findViewById<Button>(R.id.networks_scan)
        startScan.setOnClickListener { onStartScan() }

        val list = view.findViewById<RecyclerView>(R.id.list)
        list.adapter = listAdapter
        list.layoutManager = LinearLayoutManager(requireContext())

        val swipeLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        swipeLayout.setOnRefreshListener { refresh() }
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorAccent))

        NetworksViewModel.from(this).scanResults.observe(this, Observer {
            view.findViewById<View>(R.id.networks_scan_block).visibility = View.GONE
            swipeLayout.visibility = View.VISIBLE
            swipeLayout.isRefreshing = false

            val wifiMan = context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMan.connectionInfo
            listAdapter.currentNetwork = wifiInfo.getSsid()
            listAdapter.update(it)
        })
    }

    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(scanResultHandler, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(scanResultHandler)
    }

    private fun onStartScan() {
        val wifiMan = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiMan.isScanAlwaysAvailable && !wifiMan.isWifiEnabled) {
            MaterialDialog.Builder(requireContext())
                    .title(R.string.dialog_error)
                    .content(R.string.networks_start_scan_wifi_disabled)
                    .icon(requireContext().getDrawable(R.drawable.baseline_error_outline_24))
                    .positiveText(R.string.action_ok)
                    .build().show()
            return
        }

        Timber.d("Start network scanning")

        // Network scanning requires access to device location, but we first need to check
        // whether the user has given its permission to use it.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            MaterialDialog.Builder(requireContext())
                    .title(R.string.networks_start_scan)
                    .content(R.string.networks_start_scan_permission_warning)
                    .icon(requireContext().getDrawable(R.drawable.baseline_perm_scan_wifi_24))
                    .negativeText(R.string.action_cancel)
                    .positiveText(R.string.networks_start_scan_permission_warning_action)
                    .onPositive { _, _ ->
                        requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                NetworksFragment.PERMISSION_REQUEST_CODE)
                    }
                    .build().show()
        } else {
            doStartScan()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NetworksFragment.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doStartScan()
                }
            }
        }
    }

    private fun doStartScan() {
        val wifiMan = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiMan.startScan()
        Timber.d("Waiting for scanning results")
        view!!.findViewById<View>(R.id.networks_scan).visibility = View.GONE
        view!!.findViewById<View>(R.id.status_text).visibility = View.VISIBLE
    }
}
