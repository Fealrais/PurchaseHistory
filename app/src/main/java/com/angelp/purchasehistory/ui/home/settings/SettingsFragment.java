package com.angelp.purchasehistory.ui.home.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.UserClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat {
    private final String TAG = getClass().getName();
    @Inject
    UserClient userClient;
    @Inject
    PurchaseClient purchaseClient;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getActivity().setTitle(R.string.application_settings);
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
        loadAppSettings();
    }

    private void loadAppSettings() {
        Preference editCategoryPreference = findPreference("edit_category_preference");
        editCategoryPreference.setOnPreferenceClickListener((p) -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, ApplicationSettingsFragment.class, null)
                    .addToBackStack("application_settings")
                    .commit();
            return true;
        });
        SharedPreferences appPreferences = getContext().getSharedPreferences(Constants.Preferences.APP_PREFERENCES, Context.MODE_PRIVATE);
        String preferredCurrency = appPreferences.getString(Constants.Preferences.PREFERRED_CURRENCY, "");
        ListPreference currencyPreference = findPreference("currency_preference");
        currencyPreference.setValue(preferredCurrency);
        currencyPreference.setOnPreferenceChangeListener((a, value) -> appPreferences.edit()
                .putString(Constants.Preferences.PREFERRED_CURRENCY, value.toString()).commit());
    }

}