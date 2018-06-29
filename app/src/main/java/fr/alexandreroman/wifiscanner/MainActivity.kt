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

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import fr.alexandreroman.wifiscanner.nav.NavPagerAdapter
import timber.log.Timber
import java.util.*

/**
 * Main activity.
 * @author Alexandre Roman
 */
class MainActivity : AppCompatActivity() {
    private lateinit var navBar: AHBottomNavigation
    private val tabHistory: Stack<Int> = Stack()
    private var saveTabHistory: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup navigation bar.
        navBar = findViewById(R.id.navigation)
        navBar.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        // Load navigation items.
        val navBarAdapter = AHBottomNavigationAdapter(this, R.menu.navigation_items)
        navBarAdapter.setupWithBottomNavigation(navBar)

        // Setup tab history.
        val navPager = findViewById<ViewPager>(R.id.view_pager)
        navPager.offscreenPageLimit = 3
        navPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (saveTabHistory) {
                    tabHistory.push(position)
                }
            }
        })

        // Setup navigation between tabs.
        val navPagerAdapter = NavPagerAdapter(supportFragmentManager)
        navPager.adapter = navPagerAdapter
        navBar.setOnTabSelectedListener { position, wasSelected ->
            val curTab = navPagerAdapter.getCurrentFragment()
            if (wasSelected) {
                curTab.refresh()
                false
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
        tabHistory.push(0)
        saveTabHistory = true

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    override fun onBackPressed() {
        if (!tabHistory.isEmpty()) {
            /// Remove current tab index since we're going backward.
            tabHistory.pop()

            if (tabHistory.isEmpty()) {
                // The tab history is empty: our journey has come to an end.
                finish()
            } else {
                // This is the new tab index to show.
                val tabIndex = tabHistory.peek()
                saveTabHistory = false
                navBar.currentItem = tabIndex
                saveTabHistory = true
            }
        } else {
            finish()
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
        val aboutContent = layoutInflater.inflate(R.layout.fragment_about, null, false)
        val versionView = aboutContent.findViewById<TextView>(R.id.about_version)
        versionView.text = getString(R.string.about_version).format(getApplicationVersion())

        val aboutSourceCodeView = aboutContent.findViewById<TextView>(R.id.about_source_code)
        aboutSourceCodeView.movementMethod = LinkMovementMethod.getInstance()

        MaterialDialog.Builder(this)
                .customView(aboutContent, false)
                .build().show()
    }

    private fun getApplicationVersion(): String {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        return pInfo.versionName
    }

    private fun doLicenses() {
        Timber.d("Showing software licenses")
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.menu_licenses))
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }
}
