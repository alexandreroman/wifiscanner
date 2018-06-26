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

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import timber.log.Timber

/**
 * Main activity.
 * @author Alexandre Roman
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup navigation bar.
        val navBar = findViewById<AHBottomNavigation>(R.id.navigation)
        navBar.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        // Load navigation items.
        val navBarAdapter = AHBottomNavigationAdapter(this, R.menu.navigation_items)
        navBarAdapter.setupWithBottomNavigation(navBar)

        // Setup navigation between tabs.
        val navPager = findViewById<ViewPager>(R.id.view_pager)
        navPager.offscreenPageLimit = 3
        val navPagerAdapter = NavPagerAdapter(supportFragmentManager)
        navPager.adapter = navPagerAdapter
        navBar.setOnTabSelectedListener { position, wasSelected ->
            val curTab = navPagerAdapter.getCurrentFragment()
            if (wasSelected) {
                curTab.refresh()
                true
            } else {
                if (position == 3) {
                    showMenu()
                    false
                } else {
                    navPager.setCurrentItem(position, false)
                    true
                }
            }
        }
    }

    private fun showMenu() {
        Timber.d("Showing menu")

        val menuDialog = BottomSheetBuilder(this, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .addItem(R.string.menu_settings, R.string.menu_settings, R.drawable.outline_settings_24)
                .addItem(R.string.menu_about, R.string.menu_about, R.drawable.outline_info_24)
                .addDividerItem()
                .addItem(R.string.menu_licenses, R.string.menu_licenses, null)
                .expandOnStart(true)
                .setItemClickListener {
                    when (it.itemId) {
                        R.string.menu_about -> doAbout()
                        R.string.menu_settings -> doSettings()
                        R.string.menu_licenses -> doLicenses()
                    }
                }
                .createDialog()
        menuDialog.show()
    }

    private fun doSettings() {
        Timber.d("Showing app settings")
    }

    private fun doAbout() {
        Timber.d("Displaying information about this app")
    }

    private fun doLicenses() {
        Timber.d("Showing software licenses")
    }
}
