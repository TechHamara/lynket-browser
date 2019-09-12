package arun.com.chromer.browsing.backgroundloading

import android.app.Activity
import android.app.Application
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.webview.EmbeddableWebViewActivity
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.util.ActivityLifeCycleCallbackAdapter
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Singleton

typealias UnRegisterAction = () -> Unit

private fun Application.safeActivityStarted(action: (Activity, UnRegisterAction) -> Unit) {
    registerActivityLifecycleCallbacks(object : ActivityLifeCycleCallbackAdapter() {
        override fun onActivityStarted(activity: Activity?) {
            super.onActivityStarted(activity)
            val unRegisterAction: UnRegisterAction = {
                unregisterActivityLifecycleCallbacks(this)
            }
            activity?.let { startedActivity ->
                try {
                    action(startedActivity, unRegisterAction)
                } catch (e: Exception) {
                    Timber.e(e)
                    unRegisterAction()
                }
            }
        }
    })
}

abstract class BrowsingBackgroundLoadingStrategy(
        private val application: Application,
        private val schedulerProvider: SchedulerProvider
) : BackgroundLoadingStrategy {

    abstract val activityClasses: List<Class<out Activity>>

    override fun perform(url: String) {
        application.safeActivityStarted { startedActivity, unRegisterAction ->
            if (activityClasses.any { it.isAssignableFrom(startedActivity.javaClass) }) {
                val activityUrl = startedActivity.intent?.dataString
                if (url == activityUrl) {
                    schedulerProvider.ui.scheduleDirect({
                        startedActivity.moveTaskToBack(true)
                        Timber.d("Moved $activityUrl to back")
                        unRegisterAction()
                    }, 100, MILLISECONDS)
                }
            }
        }
    }
}

@Singleton
class WebViewBackgroundLoadingStrategy
@Inject
constructor(
        application: Application,
        schedulerProvider: SchedulerProvider
) : BrowsingBackgroundLoadingStrategy(application, schedulerProvider) {
    override val activityClasses: List<Class<out Activity>> = listOf(
            WebViewActivity::class.java,
            EmbeddableWebViewActivity::class.java
    )
}

@Singleton
class CustomTabBackgroundLoadingStrategy
@Inject
constructor(
        application: Application,
        schedulerProvider: SchedulerProvider
) : BrowsingBackgroundLoadingStrategy(application, schedulerProvider) {
    override val activityClasses: List<Class<out Activity>> = listOf(
            CustomTabActivity::class.java
    )
}
