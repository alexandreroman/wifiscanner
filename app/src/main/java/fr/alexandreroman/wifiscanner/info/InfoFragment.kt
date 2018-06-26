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

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    override fun refresh() {
        Timber.d("Refreshing tab: info")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<RecyclerView>(R.id.list)
        // TODO
    }
}
