package com.angelp.purchasehistory.ui.home.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
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
    private EditTextPreference monthlyLimitValuePref;
    private EditTextPreference monthlyLimitLabelPref;
    private ListPreference currencyPreference;
    private Preference editCategoryPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getActivity().setTitle(R.string.application_settings);
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
        loadAppSettings();
    }

    private void loadAppSettings() {
        editCategoryPreference = findPreference("edit_category_preference");
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
        currencyPreference = findPreference("currency_preference");
        currencyPreference.setValue(preferredCurrency);
        currencyPreference.setOnPreferenceChangeListener((a, value) -> appPreferences.edit()
                .putString(Constants.Preferences.PREFERRED_CURRENCY, value.toString()).commit());

        String monthlyLimitLabel = appPreferences.getString(Constants.Preferences.MONTHLY_LIMIT_LABEL, "");
        float monthlyLimitValue = appPreferences.getFloat(Constants.Preferences.MONTHLY_LIMIT_VALUE, -1f);

        monthlyLimitValuePref = findPreference(Constants.Preferences.MONTHLY_LIMIT_VALUE);
        monthlyLimitLabelPref = findPreference(Constants.Preferences.MONTHLY_LIMIT_LABEL);

        updateValue(monthlyLimitValue, monthlyLimitLabel);
        monthlyLimitValuePref.setOnPreferenceChangeListener((a, value) -> {
            try {
                float result = Float.parseFloat((String) value);
                SharedPreferences.Editor editor = appPreferences.edit()
                        .putFloat(Constants.Preferences.MONTHLY_LIMIT_VALUE, result);
                updateValue(result, monthlyLimitLabel);
                return editor.commit();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), R.string.invalid_input_repeat, Toast.LENGTH_LONG).show();
                return false;
            }

        });
        monthlyLimitLabelPref.setOnPreferenceChangeListener((a, value) -> {
            SharedPreferences.Editor editor = appPreferences.edit().putString(Constants.Preferences.MONTHLY_LIMIT_LABEL, value.toString());
            updateValue(monthlyLimitValue, value.toString());
            return editor.commit();
        });
    }

    private void updateValue(float monthlyLimitValue, String monthlyLimitLabel) {
        monthlyLimitValuePref.setText(Float.toString(monthlyLimitValue));
        monthlyLimitLabelPref.setText(monthlyLimitLabel);

        monthlyLimitLabelPref.setTitle(getString(R.string.label) + ": " + monthlyLimitLabel);
        monthlyLimitValuePref.setTitle(getString(R.string.value) + ": " + (monthlyLimitValue < 0 ? getString(R.string.none) : monthlyLimitValue));
    }

}