/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.intro

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.di.app.appComponent
import arun.com.chromer.intro.fragments.*
import arun.com.chromer.shared.base.ProvidesActivityComponent
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.Utils
import com.github.paolorotolo.appintro.AppIntro

/**
 * Created by Arun on 17/12/2015.
 */
class ChromerIntroActivity : AppIntro(), ProvidesActivityComponent {

  override lateinit var activityComponent: ActivityComponent

  public override fun onCreate(savedInstanceState: Bundle?) {
    activityComponent = application
      .appComponent()
      .activityComponentFactory()
      .create(this).also(::inject)
    super.onCreate(savedInstanceState)

    val bgColor = ContextCompat.getColor(this, R.color.tutorialBackgrounColor)

    addSlide(
      AppIntroFragment.newInstance(
        getString(R.string.app_name),
        getString(R.string.intro_1),
        R.drawable.chromer_hd_icon,
        bgColor
      )
    )

    addSlide(SlideOverExplanationFragment())

    addSlide(ProviderSelectionIntroFragment())

    addSlide(WebHeadsIntroFragment())

    addSlide(
      AppIntroFragment.newInstance(
        getString(R.string.amp),
        getString(R.string.tutorial_amp_intro),
        R.drawable.tutorial_amp_mode,
        bgColor
      )
    )

    addSlide(ArticleIntroFragment())

    if (Utils.ANDROID_LOLLIPOP) {
      addSlide(
        AppIntroFragment.newInstance(
          getString(R.string.merge_tabs),
          getText(R.string.merge_tabs_explanation_intro),
          R.drawable.tutorial_merge_tabs_and_apps,
          bgColor
        )
      )
    }

    addSlide(
      AppIntroFragment.newInstance(
        getString(R.string.per_app_settings),
        getText(R.string.per_app_settings_explanation),
        R.drawable.tutorial_per_app_settings,
        bgColor
      )
    )

    setColorTransitionsEnabled(true)
    showStatusBar(false)
    showSkipButton(true)
    isProgressButtonEnabled = true

    window.statusBarColor = ColorUtil.getDarkenedColorForStatusBar(bgColor)
  }

  override fun onSkipPressed(currentFragment: Fragment?) {
    super.onSkipPressed(currentFragment)
    finish()
  }

  override fun onDonePressed(currentFragment: Fragment?) {
    super.onDonePressed(currentFragment)
    finish()
  }

  override fun inject(activityComponent: ActivityComponent) {
    activityComponent.inject(this)
  }
}