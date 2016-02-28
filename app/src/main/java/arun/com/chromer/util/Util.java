package arun.com.chromer.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.MainActivity;
import arun.com.chromer.R;
import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.chrometabutilites.CustomTabHelper;
import arun.com.chromer.model.App;

/**
 * Created by Arun on 17/12/2015.
 */
public class Util {

    public final static CustomActivityHelper.CustomTabsFallback CUSTOM_TABS_FALLBACK =
            new CustomActivityHelper.CustomTabsFallback() {
                @Override
                public void openUri(Activity activity, Uri uri) {
                    if (activity != null) {
                        Toast.makeText(activity,
                                activity.getString(R.string.fallback_msg),
                                Toast.LENGTH_SHORT).show();
                        try {
                            activity.startActivity(Intent.createChooser(
                                    new Intent(Intent.ACTION_VIEW, uri),
                                    activity.getString(R.string.open_with)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity,
                                    activity.getString(R.string.unxp_err), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            };

    public static void openPlayStore(Context context, String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    public static List<String> findURLs(String string) {
        if (string == null) {
            return null;
        }
        List<String> links = new ArrayList<>();
        Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(string);
        while (m.find()) {
            String url = m.group();
            if (!url.toLowerCase().matches("^\\w+://.*")) {
                url = "http://" + url;
            }
            links.add(url);
        }

        return links;
    }

    public static String getPackageVersion(Context context) {
        String versionName;
        try {
            versionName = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
        return versionName;
    }

    public static boolean isPackageInstalled(Context c, String packagename) {
        PackageManager pm = c.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getAppNameWithPackage(Context context, String pack) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(pack, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        int accesEnbld = 0;
        final String service = BuildConfig.APPLICATION_ID + "/arun.com.chromer.services.ScannerService";
        try {
            accesEnbld = Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');

        if (accesEnbld == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String aService = splitter.next();
                    if (aService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getDefaultBrowserPackage(Context context) {
        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.GOOGLE_URL));
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(launchIntent,
                PackageManager.MATCH_DEFAULT_ONLY);

        return resolveInfo != null ? resolveInfo.activityInfo.packageName : "";
    }

    public static List<App> getCustomTabApps(Context context) {
        List<App> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabHelper.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                String packageName = info.activityInfo.packageName;
                if (packageName.equalsIgnoreCase(context.getPackageName()))
                    continue;
                apps.add(new App(context, packageName));
            }
        }
        return apps;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean canReadUsageStats(Context context) {
        // http://stackoverflow.com/questions/27215013/check-if-my-application-has-usage-access-enabled
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String processSearchText(String text) {
        if (text == null) return null;
        if (Patterns.WEB_URL.matcher(text).matches()) {
            if (!text.toLowerCase().matches("^\\w+://.*")) {
                text = "http://" + text;
            }
            return text;
        } else
            return StringConstants.SEARCH_URL + text.replace(" ", "+");
    }

    public static boolean isVoiceRecognizerPresent(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities != null) && (activities.size() > 0);
    }

    public static int dpToPx(int dp) {
        // resources instead of context !!
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * A helper class for providing a shadow on sheets
     */
    @TargetApi(21)
    public static class ShadowOutline extends ViewOutlineProvider {

        int width;
        int height;

        public ShadowOutline(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, width, height);
        }
    }

    public static int getForegroundTextColor(int backgroundColor) {
        final int whiteColorAlpha = ColorUtils.calculateMinimumAlpha(Color.WHITE, backgroundColor, 4.5f);

        if (whiteColorAlpha != -1) {
            return ColorUtils.setAlphaComponent(Color.WHITE, whiteColorAlpha);
        }

        final int blackColorAlpha = ColorUtils.calculateMinimumAlpha(Color.BLACK, backgroundColor, 4.5f);

        if (blackColorAlpha != -1) {
            return ColorUtils.setAlphaComponent(Color.BLACK, blackColorAlpha);
        }

        return whiteColorAlpha != -1 ? ColorUtils.setAlphaComponent(Color.WHITE, whiteColorAlpha)
                : ColorUtils.setAlphaComponent(Color.BLACK, blackColorAlpha);
    }
}
