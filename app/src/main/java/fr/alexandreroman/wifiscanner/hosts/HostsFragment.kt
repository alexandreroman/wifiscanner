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

package fr.alexandreroman.wifiscanner.hosts

import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import fr.alexandreroman.wifiscanner.R
import fr.alexandreroman.wifiscanner.nav.NavFragment
import timber.log.Timber

/**
 * Fragment displaying the "hosts" tab.
 * @author Alexandre Roman
 */
class HostsFragment : NavFragment() {
    companion object {
        @JvmStatic
        fun newInstance(): HostsFragment {
            return HostsFragment()
        }
    }

    override fun refresh() {
        Timber.d("Refreshing tab: hosts")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hosts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startScanButton = view.findViewById<Button>(R.id.hosts_scan)
        startScanButton.setOnClickListener { onStartScan() }
    }

    private fun onStartScan() {
        val progressView = layoutInflater.inflate(R.layout.fragment_hosts_progress, null)
        val progressDialog = BottomSheetDialog(requireContext(), R.style.AppTheme_ProgressBottomSheetDialog)
        progressDialog.setContentView(progressView)

        val stopButton = progressView.findViewById<Button>(R.id.hosts_stop_scan)
        stopButton.setOnClickListener { progressDialog.cancel() }

        val startScanButton = view!!.findViewById<Button>(R.id.hosts_scan)
        startScanButton.isEnabled = false

        val nd = NetworkDiscoverer({})
        progressDialog.setOnShowListener { nd.start() }

        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setOnCancelListener {
            nd.stop()
            startScanButton.isEnabled = true
        }

        progressDialog.show()
    }
}
