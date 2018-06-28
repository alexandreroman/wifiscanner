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

package fr.alexandreroman.wifiscanner.info

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import fr.alexandreroman.wifiscanner.R
import fr.alexandreroman.wifiscanner.nav.NavFragment
import timber.log.Timber

/**
 * Fragment displaying the "info" tab.
 * @author Alexandre Roman
 */
class InfoFragment : NavFragment() {
    companion object {
        @JvmStatic
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }

        private const val PERMISSION_REQUEST_CODE = 42
    }

    private val wifiNetHandler = object : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network?, linkProperties: LinkProperties?) {
            refreshUI()
        }

        override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) {
            refreshUI()
        }

        override fun onLost(network: Network?) {
            refreshUI()
        }

        private fun refreshUI() {
            Handler(Looper.getMainLooper()).post { refresh() }
        }
    }

    override fun refresh() {
        Timber.d("Refreshing tab: info")

        val viewModel = InfoViewModel.from(this)
        viewModel.update(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        swipeLayout.setOnRefreshListener { refresh() }
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorAccent))

        val listAdapter = WifiInfoAdapter()
        val listView = view.findViewById<RecyclerView>(R.id.list)
        listView.adapter = listAdapter
        listView.layoutManager = LinearLayoutManager(context)

        val statusText = view.findViewById<TextView>(R.id.status_text)

        val viewModel = InfoViewModel.from(this)
        viewModel.wifiInfo.observe(this, Observer {
            listAdapter.update(it)

            val reviewPerms = view.findViewById<Button>(R.id.info_review_permissions)
            reviewPerms?.setOnClickListener { onReviewPermissions() }

            swipeLayout.isRefreshing = false
            if (it == null) {
                swipeLayout.visibility = View.GONE
                statusText.visibility = View.VISIBLE
                statusText.setText(R.string.info_no_wifi)
            } else {
                swipeLayout.visibility = View.VISIBLE
                statusText.visibility = View.GONE
            }
        })
    }

    override fun onStart() {
        super.onStart()
        refresh()

        val connMan = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiReq = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        connMan.registerNetworkCallback(wifiReq, wifiNetHandler)
    }

    override fun onStop() {
        super.onStop()
        val connMan = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMan.unregisterNetworkCallback(wifiNetHandler)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refresh()
                }
            }
        }
    }

    private fun onReviewPermissions() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
    }
}
