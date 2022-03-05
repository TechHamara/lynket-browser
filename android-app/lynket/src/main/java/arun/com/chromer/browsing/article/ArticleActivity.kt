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

package arun.com.chromer.browsing.article

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.article.adapter.ArticleAdapter
import arun.com.chromer.browsing.article.util.ArticleScrollListener
import arun.com.chromer.browsing.article.util.ArticleUtil.changeProgressBarColors
import arun.com.chromer.browsing.article.util.ArticleUtil.changeRecyclerOverscrollColors
import arun.com.chromer.browsing.article.view.ElasticDragDismissFrameLayout
import arun.com.chromer.browsing.menu.MenuDelegate
import arun.com.chromer.data.Result
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.extenstions.watch
import arun.com.chromer.search.provider.SearchProviders
import arun.com.chromer.settings.Preferences.*
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.Utils
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.RequestManager
import com.jakewharton.rxbinding.widget.RxSeekBar
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import hu.akarnokd.rxjava.interop.RxJavaInterop
import kotlinx.android.synthetic.main.activity_article_mode.*
import javax.inject.Inject

class ArticleActivity : BrowsingActivity() {
  override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

  private lateinit var browsingArticleViewModel: BrowsingArticleViewModel

  private var url: String? = null

  override val layoutRes: Int get() = R.layout.activity_article_mode

  private var primaryColor: Int = 0
  private var accentColor: Int = 0

  private lateinit var articleAdapter: ArticleAdapter

  private var articleScrollListener: ArticleScrollListener? = null

  @Inject
  lateinit var tabsManager: TabsManager

  @Inject
  lateinit var menuDelegate: MenuDelegate

  @Inject
  lateinit var requestManager: RequestManager

  @Inject
  lateinit var searchProviders: SearchProviders

  private val textSizeIcon: IconicsDrawable by lazy {
    IconicsDrawable(this)
      .icon(CommunityMaterial.Icon.cmd_format_size)
      .color(ContextCompat.getColor(this, R.color.article_secondaryText))
      .sizeDp(24)
  }

  private val dismissIcon: IconicsDrawable by lazy {
    IconicsDrawable(this)
      .icon(CommunityMaterial.Icon.cmd_close)
      .color(ContextCompat.getColor(this, R.color.article_secondaryText))
      .sizeDp(24)
  }

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readCustomizations()
    url = intent.dataString

    setupToolbar()
    setupCloseListeners()
    setupBottombar()
    setupTheme()

    articleScrollListener = ArticleScrollListener(
      toolbar,
      statusBar,
      primaryColor
    ).also(recyclerView::addOnScrollListener)
    recyclerView.addOnScrollListener(systemUiLowProfileOnScrollListener)

    observeViewModel()
    if (savedInstanceState == null) {
      browsingArticleViewModel.loadArticle(url!!)
    }
  }

  private fun observeViewModel() {
    browsingArticleViewModel =
      ViewModelProviders.of(this, viewModelFactory).get(BrowsingArticleViewModel::class.java)
    browsingArticleViewModel.articleLiveData.watch(this) { result ->
      when (result) {
        is Result.Success -> {
          val webArticle = result.data
          if (webArticle == null) {
            onArticleLoadingFailed()
          } else {
            onArticleLoaded(webArticle)
          }
        }
        is Result.Failure -> onArticleLoadingFailed()
        else -> {
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    setLowProfileSystemUi()
  }

  override fun onWebsiteLoaded(website: Website) {}

  override fun onToolbarColorSet(websiteThemeColor: Int) {
    primaryColor = websiteThemeColor
    accentColor = ContextCompat.getColor(this, R.color.accent)

    changeRecyclerOverscrollColors(recyclerView, primaryColor)
    changeProgressBarColors(progressBar, primaryColor)
    articleScrollListener?.setPrimaryColor(primaryColor)

    if (preferences.dynamiceToolbarEnabledAndWebEnabled() && canUseAsAccentColor(primaryColor)) {
      accentColor = primaryColor
    }

    if (::articleAdapter.isInitialized) {
      articleAdapter.setAccentColor(accentColor)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu) = menuDelegate.createOptionsMenu(menu)

  override fun onPrepareOptionsMenu(menu: Menu) = menuDelegate.prepareOptionsMenu(menu)

  override fun onOptionsItemSelected(item: MenuItem) = if (item.itemId == R.id.menu_text_size) {
    TransitionManager.beginDelayedTransition(articleBottomLinearLayout)
    articleTextSizeCard.show()
    true
  } else menuDelegate.handleItemSelected(item.itemId)

  private fun onArticleLoadingFailed() {
    hideLoading()
    // Loading failed, try to go back to normal url tab if it exists, else start a new normal
    // rendering tab.
    finish()
    Toast.makeText(this, R.string.article_loading_failed, Toast.LENGTH_SHORT).show()
    tabsManager.openBrowsingTab(
      this,
      Website(intent.dataString!!),
      smart = true,
      fromNewTab = false,
      activityNames = TabsManager.FULL_BROWSING_ACTIVITIES
    )
  }

  private fun onArticleLoaded(webArticle: WebArticle) {
    if (webArticle.elements != null && webArticle.elements.size >= MIN_NUM_ELEMENTS) {
      renderArticle(webArticle)
    } else {
      onArticleLoadingFailed()
    }
  }

  private fun setupToolbar() {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.article_ic_close)
    supportActionBar?.title = null
  }

  private fun hideLoading() {
    progressBar.gone()
  }

  private fun setupCloseListeners() {
    transparentSide1.setOnClickListener { finish() }
    transparentSide2.setOnClickListener { finish() }
    dragDismissLayout.addListener(
      object : ElasticDragDismissFrameLayout.ElasticDragDismissCallback() {
        override fun onDragDismissed() {
          finish()
        }
      })
  }


  private fun setupBottombar() {
    val bg = ContextCompat.getColor(this, R.color.article_windowBackground)
    bottomNavigation.background = ColorDrawable(bg)
    menuDelegate.setupBottombar(bottomNavigation)

    // Text size related
    textSizeIconView.setImageDrawable(textSizeIcon)
    textSizeDismiss.setImageDrawable(dismissIcon)
    textSizeSeekbar.progress = preferences.articleTextSizeIncrement()
    subs.add(RxSeekBar
      .changes(textSizeSeekbar)
      .skip(1)
      .subscribe { size ->
        recyclerView.post {
          if (::articleAdapter.isInitialized) {
            articleAdapter.textSizeIncrementSp = size
          }
        }
      })
  }

  private fun setupTheme() {
    when (preferences.articleTheme()) {
      THEME_BLACK -> handleBlackTheme()
      THEME_DARK -> setNavigationBarColor(
        ContextCompat.getColor(
          this,
          R.color.article_windowBackground
        )
      )
    }
    setLowProfileSystemUi()
  }

  private fun setLowProfileSystemUi() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
  }

  private fun handleBlackTheme() {
    coordinatorLayout.setBackgroundColor(Color.BLACK)
    bottomNavigation.setBackgroundColor(Color.BLACK)
    articleTextSizeCard.setBackgroundColor(Color.BLACK)
    setNavigationBarColor(Color.BLACK)
  }

  private fun setNavigationBarColor(@ColorInt color: Int) {
    if (Utils.ANDROID_LOLLIPOP) {
      window.navigationBarColor = color
    }
  }

  private fun canUseAsAccentColor(primaryColor: Int): Boolean {
    val isDark = preferences.articleTheme() != THEME_LIGHT
    return if (isDark) {
      !ColorUtil.shouldUseLightForegroundOnBackground(primaryColor)
    } else {
      ColorUtil.shouldUseLightForegroundOnBackground(primaryColor)
    }
  }


  private fun readCustomizations() {
    when (preferences.articleTheme()) {
      THEME_LIGHT -> delegate.localNightMode = MODE_NIGHT_NO
      THEME_DARK, THEME_BLACK -> delegate.localNightMode = MODE_NIGHT_YES
      else -> delegate.localNightMode = MODE_NIGHT_AUTO_BATTERY
    }
  }

  private fun renderArticle(webArticle: WebArticle) {
    articleAdapter = ArticleAdapter(
      webArticle,
      accentColor,
      requestManager,
      preferences.articleTextSizeIncrement()
    ).apply {
      setElements(webArticle.elements)
      RxJavaInterop.toV2Observable(keywordsClicks())
        .switchMap { key ->
          browsingArticleViewModel
            .selectedSearchProvider
            .map { searchProvider -> searchProvider.getSearchUrl(key) }
        }.takeUntil(lifecycleEvents.destroys)
        .subscribe { url ->
          tabsManager.openUrl(this@ArticleActivity, Website(url))
        }
    }
    recyclerView.apply {
      layoutManager = LinearLayoutManager(this@ArticleActivity)
      adapter = articleAdapter
    }
    hideLoading()
  }

  @OnClick(R.id.textSizeDismiss)
  fun onTextSizeDismiss() {
    TransitionManager.beginDelayedTransition(articleBottomLinearLayout)
    articleTextSizeCard.gone()

    val currentIncrement = textSizeSeekbar.progress
    if (currentIncrement != preferences.articleTextSizeIncrement()) {
      with(MaterialDialog.Builder(this)) {
        title(R.string.save_size_dialog_title)
        content(R.string.save_size_dialog_content)
        positiveText(android.R.string.yes)
        negativeText(android.R.string.no)
        onPositive { _, _ -> preferences.articleTextSizeIncrement(textSizeSeekbar.progress) }
        build()
      }.show()
    }
  }


  private val systemUiLowProfileOnScrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      setLowProfileSystemUi()
    }
  }

  companion object {
    private const val MIN_NUM_ELEMENTS = 1
  }
}
