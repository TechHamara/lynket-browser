package arun.com.chromer.activities.settings.preferences;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.lookandfeel.LookAndFeelActivity;
import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import arun.com.chromer.activities.settings.widgets.ColorPreference;
import arun.com.chromer.activities.settings.widgets.IconListPreference;
import arun.com.chromer.activities.settings.widgets.IconSwitchPreference;
import arun.com.chromer.shared.AppDetectService;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;

import static arun.com.chromer.shared.Constants.ACTION_TOOLBAR_COLOR_SET;

public class PersonalizationPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String[] PREFERENCE_GROUP = new String[]{
            Preferences.ANIMATION_SPEED,
            Preferences.ANIMATION_TYPE,
            Preferences.PREFERRED_ACTION,
            Preferences.TOOLBAR_COLOR
    };

    private final IntentFilter toolbarColorSetFilter = new IntentFilter(ACTION_TOOLBAR_COLOR_SET);

    private IconSwitchPreference dynamicColorPreference;
    private IconSwitchPreference coloredToolbarPreference;
    private ColorPreference toolbarColorPreference;
    private IconListPreference animationSpeedPreference;
    private IconListPreference openingAnimationPreference;
    private IconListPreference preferredActionPreference;

    public PersonalizationPreferenceFragment() {
        // Required empty public constructor
    }

    public static PersonalizationPreferenceFragment newInstance() {
        final PersonalizationPreferenceFragment fragment = new PersonalizationPreferenceFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.personalization_preferences);
        // init and set icon
        init();
        setupIcons();

        // setup preferences after creation
        setupToolbarColorPreference();
        setupDynamicToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mColorSelectionReceiver, toolbarColorSetFilter);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceStates(Preferences.TOOLBAR_COLOR_PREF);
        updatePreferenceStates(Preferences.ANIMATION_TYPE);
        updatePreferenceSummary(PREFERENCE_GROUP);
    }

    @Override
    public void onPause() {
        unregisterReceiver(mColorSelectionReceiver);
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceStates(key);
        updatePreferenceSummary(PREFERENCE_GROUP);
    }

    private void init() {
        dynamicColorPreference = (IconSwitchPreference) findPreference(Preferences.DYNAMIC_COLOR);
        coloredToolbarPreference = (IconSwitchPreference) findPreference(Preferences.TOOLBAR_COLOR_PREF);
        toolbarColorPreference = (ColorPreference) findPreference(Preferences.TOOLBAR_COLOR);
        preferredActionPreference = (IconListPreference) findPreference(Preferences.PREFERRED_ACTION);
        openingAnimationPreference = (IconListPreference) findPreference(Preferences.ANIMATION_TYPE);
        animationSpeedPreference = (IconListPreference) findPreference(Preferences.ANIMATION_SPEED);
    }

    private void setupIcons() {
        final Drawable palette = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_palette)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24);
        toolbarColorPreference.setIcon(palette);
        coloredToolbarPreference.setIcon(palette);
        dynamicColorPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_format_color_fill)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        preferredActionPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_heart)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        openingAnimationPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_image_filter_none)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        animationSpeedPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_speedometer)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
    }


    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(Preferences.TOOLBAR_COLOR_PREF)) {
            final boolean coloredToolbar = Preferences.get(getContext()).isColoredToolbar();
            enableDisablePreference(coloredToolbar, Preferences.TOOLBAR_COLOR, Preferences.DYNAMIC_COLOR);
        } else if (key.equalsIgnoreCase(Preferences.ANIMATION_TYPE)) {
            final boolean animationEnabled = Preferences.get(getContext()).isAnimationEnabled();
            enableDisablePreference(animationEnabled, Preferences.ANIMATION_SPEED);
        }
        updateDynamicSummary();
    }

    private void updateDynamicSummary() {
        dynamicColorPreference.setSummary(Preferences.get(getContext()).dynamicColorSummary());
        boolean isColoredToolbar = Preferences.get(getContext()).isColoredToolbar();
        if (!isColoredToolbar) {
            dynamicColorPreference.setChecked(false);
        }
    }

    private void setupDynamicToolbar() {
        dynamicColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                final boolean isChecked = switchCompat.isChecked();
                if (isChecked) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.dynamic_toolbar_color)
                            .content(R.string.dynamic_toolbar_help)
                            .items(getString(R.string.based_on_app),
                                    getString(R.string.based_on_web))
                            .positiveText(android.R.string.ok)
                            .alwaysCallMultiChoiceCallback()
                            .itemsCallbackMultiChoice(Preferences.get(getContext()).dynamicToolbarSelections(),
                                    new MaterialDialog.ListCallbackMultiChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog,
                                                                   Integer[] which,
                                                                   CharSequence[] text) {
                                            if (which.length == 0) {
                                                switchCompat.setChecked(false);
                                                Preferences.get(getContext()).dynamicToolbar(false);
                                            } else switchCompat.setChecked(true);

                                            Preferences.get(getContext()).updateAppAndWeb(which);
                                            requestUsagePermissionIfNeeded();
                                            handleAppDetectionService();
                                            updateDynamicSummary();
                                            return true;
                                        }
                                    })
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    switchCompat.setChecked(Preferences.get(getContext()).dynamicToolbar());
                                }
                            })
                            .show();
                    requestUsagePermissionIfNeeded();
                }
                handleAppDetectionService();
                updateDynamicSummary();
                return false;
            }
        });
    }


    private void setupToolbarColorPreference() {
        toolbarColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int chosenColor = ((ColorPreference) preference).getColor();
                new ColorChooserDialog.Builder((LookAndFeelActivity) getActivity(), R.string.default_toolbar_color)
                        .titleSub(R.string.default_toolbar_color)
                        .allowUserColorInputAlpha(false)
                        .preselect(chosenColor)
                        .dynamicButtonColor(false)
                        .show();
                return true;
            }
        });
    }

    private void requestUsagePermissionIfNeeded() {
        if (Preferences.get(getContext()).dynamicToolbarOnApp() && !Utils.canReadUsageStats(getActivity())) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.permission_required)
                    .content(R.string.usage_permission_explanation_appcolor)
                    .positiveText(R.string.grant)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            getActivity().startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        }
                    }).show();
        }

    }

    private void handleAppDetectionService() {
        if (ServiceUtil.isAppBasedToolbarColor(getActivity()) || Preferences.get(getContext()).blacklist())
            getActivity().startService(new Intent(getActivity().getApplicationContext(), AppDetectService.class));
        else
            getActivity().stopService(new Intent(getActivity().getApplicationContext(), AppDetectService.class));
    }

    private final BroadcastReceiver mColorSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int selectedColor = intent.getIntExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, Constants.NO_COLOR);
            if (selectedColor != Constants.NO_COLOR) {
                ColorPreference preference = (ColorPreference) findPreference(Preferences.TOOLBAR_COLOR);
                if (preference != null) {
                    preference.setColor(selectedColor);
                }
            }
        }
    };
}
