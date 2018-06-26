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

package fr.alexandreroman.wifiscanner

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

class NavPagerAdapter : FragmentPagerAdapter {
    private val fragments = arrayListOf(
            InfoFragment.newInstance(),
            HostsFragment.newInstance(),
            NetworksFragment.newInstance()
    )
    private var currentFragment: NavFragment = fragments[0]

    constructor(fm: FragmentManager) : super(fm)

    override fun getItem(position: Int): NavFragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (currentFragment != `object`) {
            currentFragment = `object` as NavFragment
        }

        super.setPrimaryItem(container, position, `object`)
    }

    fun getCurrentFragment(): NavFragment {
        return currentFragment
    }
}
