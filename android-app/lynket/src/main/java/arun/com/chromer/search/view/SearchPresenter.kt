package arun.com.chromer.search.view

import android.annotation.SuppressLint
import arun.com.chromer.di.view.Detaches
import arun.com.chromer.search.provider.SearchProvider
import arun.com.chromer.search.provider.SearchProviders
import arun.com.chromer.search.suggestion.SuggestionsEngine
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionType
import arun.com.chromer.settings.RxPreferences
import com.jakewharton.rxrelay2.PublishRelay
import dev.arunkumar.android.dagger.view.PerView
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import io.reactivex.BackpressureStrategy.LATEST
import io.reactivex.Flowable
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

data class SuggestionResult(
  val query: String,
  val suggestionType: SuggestionType,
  val suggestions: List<SuggestionItem>
)

@SuppressLint("CheckResult")
@PerView
class SearchPresenter
@Inject
constructor(
  private val suggestionsEngine: SuggestionsEngine,
  private val schedulerProvider: SchedulerProvider,
  @param:Detaches
  private val detaches: Observable<Unit>,
  searchProviders: SearchProviders,
  private val rxPreferences: RxPreferences
) {
  private val suggestionsSubject = PublishRelay.create<SuggestionResult>()
  val suggestions: Observable<SuggestionResult> = suggestionsSubject.hide()

  fun registerSearch(queryObservable: Observable<String>) {
    queryObservable
      .toFlowable(LATEST)
      .debounce(200, MILLISECONDS, schedulerProvider.pool)
      .doOnNext { Timber.d(it) }
      .flatMap { query ->
        Flowable.just(query)
          .compose(suggestionsEngine.suggestionsTransformer())
          .publish(suggestionsEngine.distinctSuggestionsPublishSelector())
          .map { SuggestionResult(query, it.first, it.second) }
      }.takeUntil(detaches.toFlowable(LATEST))
      .subscribe(suggestionsSubject::accept)
  }

  fun registerSearchProviderClicks(searchProviderClicks: Observable<SearchProvider>) {
    searchProviderClicks
      .observeOn(schedulerProvider.pool)
      .map { it.name }
      .observeOn(schedulerProvider.ui)
      .takeUntil(detaches)
      .subscribe(rxPreferences.searchEngine)
  }

  val searchEngines: Observable<List<SearchProvider>> = searchProviders
    .availableProviders
    .toObservable()
    .subscribeOn(schedulerProvider.pool)
    .share()

  val selectedSearchProvider: Observable<SearchProvider> = searchProviders.selectedProvider

  fun getSearchUrl(searchUrl: String): Observable<String> {
    return selectedSearchProvider.take(1)
      .map { provider -> provider.getSearchUrl(searchUrl) }
  }
}