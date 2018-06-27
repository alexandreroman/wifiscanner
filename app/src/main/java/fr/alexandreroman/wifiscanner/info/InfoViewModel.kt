package fr.alexandreroman.wifiscanner.info

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.Fragment

/**
 * [ViewModel] implementation for "info" tab.
 * @author Alexandre Roman
 */
class InfoViewModel(val wifiInfo: LiveData<WifiInfo> = MutableLiveData()) : ViewModel() {
    companion object {
        fun from(fragment: Fragment) = ViewModelProviders.of(fragment).get(InfoViewModel::class.java)
    }

    fun update(context: Context?) {
        if (context != null) {
            (wifiInfo as MutableLiveData).postValue(WifiInfoRepository.load(context))
        }
    }
}
