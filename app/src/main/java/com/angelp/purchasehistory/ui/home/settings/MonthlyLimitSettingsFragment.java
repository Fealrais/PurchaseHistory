package com.angelp.purchasehistory.ui.home.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.web.clients.SettingsClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class MonthlyLimitSettingsFragment extends PreferenceFragmentCompat {
    @Inject
    SettingsClient settingsClient;
    private EditMonthlyLimitDialog editMonthlyLimitDialog;
    private AddMonthlyLimitDialog addMonthlyLimitDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.monthly_limit_preferences, rootKey);

        Preference addLimitPreference = findPreference("add_new_monthly_limit_preference");
        PreferenceCategory monthlyLimitCategory = findPreference("monthly_limit_preference_category");
        setupMonthlyLimitEdit(monthlyLimitCategory);

        addLimitPreference.setOnPreferenceClickListener((v)->{
            addMonthlyLimitDialog = new AddMonthlyLimitDialog((newMonthlyLimit) -> {
                Preference monthlyLimitPreference = new Preference(getContext());
                setupMonthlyLimit(newMonthlyLimit, monthlyLimitPreference);
                monthlyLimitCategory.addPreference(monthlyLimitPreference);
            });
            addMonthlyLimitDialog.show(getParentFragmentManager(), "Add_monthly_limit");
            return false;
        });


    }

    private void setupMonthlyLimitEdit(PreferenceCategory monthlyLimitCategory) {
        new Thread(() -> {
            List<MonthlyLimitView> monthlyLimits = settingsClient.getMonthlyLimits();
            for (MonthlyLimitView monthlyLimit : monthlyLimits) {
                Preference monthlyLimitPreference = new Preference(getContext());
                setupMonthlyLimit(monthlyLimit, monthlyLimitPreference);
                monthlyLimitPreference.setOnPreferenceClickListener((p) -> {
                    editMonthlyLimitDialog = new EditMonthlyLimitDialog(monthlyLimit.getId(), monthlyLimit,
                            (newMonthlyLimit) -> setupMonthlyLimit(newMonthlyLimit, monthlyLimitPreference));
                    editMonthlyLimitDialog.show(getParentFragmentManager(), "Edit_monthly_limit");
                    return false;
                });
                Preference deletePreference = new Preference(getContext());
                deletePreference.setTitle("Delete");
                deletePreference.setOnPreferenceClickListener((p) -> {
                    new Thread(() -> {
                        settingsClient.deleteMonthlyLimit(monthlyLimit.getId());
                        new Handler(Looper.getMainLooper()).post(() -> monthlyLimitCategory.removePreference(monthlyLimitPreference));
                    }).start();
                    return false;
                });
                monthlyLimitCategory.addPreference(monthlyLimitPreference);
            }
        }).start();

    }

    private void setupMonthlyLimit(MonthlyLimitView monthlyLimit, Preference monthlyLimitPreference) {
        new Handler(Looper.getMainLooper()).post(() -> {
            monthlyLimitPreference.setTitle(monthlyLimit.getLabel());
            monthlyLimitPreference.setSummary(String.valueOf(monthlyLimit.getValue()));
        });
    }
}