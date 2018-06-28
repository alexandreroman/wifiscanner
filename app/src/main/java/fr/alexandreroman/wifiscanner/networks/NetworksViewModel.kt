package fr.alexandreroman.wifiscanner.networks

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.net.wifi.ScanResult
import android.support.v4.app.Fragment
import timber.log.Timber

/**
 * [ViewModel] implementation for "networks" tab.
 * @author Alexandre Roman
 */
class NetworksViewModel(val scanResults: LiveData<List<ScanResult>> = MutableLiveData()) : ViewModel() {
    companion object {
        fun from(fragment: Fragment) = ViewModelProviders.of(fragment).get(NetworksViewModel::class.java)
    }

    fun update(newScanResults: List<ScanResult>) {
        val filteredScanResults = newScanResults
                .filter { !it.SSID.isNullOrBlank() }
                .distinctBy { it.SSID }
                .sortedWith(ScanResultComparator)
        Timber.d("Wi-Fi scan results received: %d element(s)", filteredScanResults.size)
        (scanResults as MutableLiveData).postValue(filteredScanResults)
    }

    private object ScanResultComparator : Comparator<ScanResult> {
        override fun compare(o1: ScanResult?, o2: ScanResult?): Int {
            val i = o1!!.level.compareTo(o2!!.level)
            if (i != 0) {
                // Higher signal level first.
                return -i
            }
            return o1.SSID.compareTo(o2.SSID)
        }
    }
}
