/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.perapp

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.watch
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.util.ServiceManager
import arun.com.chromer.util.Utils
import arun.com.chromer.util.viemodel.ViewModelFactory
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.acitivty_per_apps.*
import kotlinx.android.synthetic.main.activity_per_apps_content.*
import javax.inject.Inject

class PerAppSettingsActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener, Snackable {
  @Inject
  lateinit var preferences: Preferences

  @Inject
  lateinit var perAppListAdapter: PerAppListAdapter

  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private lateinit var perAppViewModel: PerAppSettingsViewModel

  override fun inject(activityComponent: ActivityComponent) {
    activityComponent.inject(this)
  }

  override val layoutRes: Int
    get() = R.layout.acitivty_per_apps

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupToolbar()
    setupList()
    observeViewModel()
  }

  private fun setupList() {
    appRecyclerView.layoutManager = LinearLayoutManager(this)
    appRecyclerView.adapter = perAppListAdapter
    (appRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
  }


  private fun observeViewModel() {
    val owner = this@PerAppSettingsActivity
    perAppViewModel =
      ViewModelProviders.of(owner, viewModelFactory).get(PerAppSettingsViewModel::class.java)
    perAppViewModel.apply {
      loadingLiveData.watch(owner) { loading(it!!) }
      appsLiveData.watch(owner) { apps ->
        perAppListAdapter.setApps(apps!!)
      }
      appLiveData.watch(owner) { appIndexPair ->
        perAppListAdapter.setApp(appIndexPair!!.first, appIndexPair.second)
      }
    }

    subs.apply {
      add(perAppListAdapter.blacklistSelections.subscribe { selections ->
        perAppViewModel.blacklist(selections)
      })
      add(perAppListAdapter.incognitoSelections.subscribe { selections ->
        perAppViewModel.incognito(selections)
      })
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    if (savedInstanceState == null) {
      loadApps()
    }
  }

  private fun setupToolbar() {
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    swipeRefreshLayout.apply {
      setColorSchemeResources(
        R.color.colorPrimary,
        R.color.colorAccent
      )
      setOnRefreshListener {
        loadApps()
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.per_apps_menu, menu)
    val menuItem = menu.findItem(R.id.blacklist_switch_item)
    if (menuItem != null) {
      val blackListSwitch = menuItem.actionView.findViewById<SwitchCompat>(R.id.blacklist_switch)
      if (blackListSwitch != null) {
        val blackListActive = preferences.perAppSettings() && Utils.canReadUsageStats(this)
        preferences.perAppSettings(blackListActive)
        blackListSwitch.isChecked = Preferences.get(this).perAppSettings()
        blackListSwitch.setOnCheckedChangeListener(this)
      }
    }
    return true
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private fun requestUsagePermission() {
    MaterialDialog.Builder(this)
      .title(R.string.permission_required)
      .content(R.string.usage_permission_explanation_per_apps)
      .positiveText(R.string.grant)
      .onPositive { _, _ -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }.show()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
    if (isChecked && !Utils.canReadUsageStats(applicationContext)) {
      buttonView.isChecked = false
      requestUsagePermission()
    } else {
      snack(if (isChecked) getString(R.string.per_apps_on) else getString(R.string.per_apps_off))
      preferences.perAppSettings(isChecked)
      ServiceManager.takeCareOfServices(applicationContext)
    }
  }


  private fun loadApps() {
    perAppViewModel.loadApps()
  }

  private fun loading(loading: Boolean) {
    swipeRefreshLayout.isRefreshing = loading
  }

  override fun snack(message: String) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
  }

  override fun snackLong(message: String) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
  }
}
