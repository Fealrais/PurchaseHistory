package com.example.purchasehistory.ui.home.settings;

import android.os.Bundle;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.R;
import com.example.purchasehistory.util.AndroidUtils;
import com.example.purchasehistory.web.clients.UserClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.Optional;

@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat {
    private final String TAG = getClass().getName();
    @Inject
    UserClient userClient;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
        loadAccountSettings();
        loadReferralSettings();
    }

    private void loadAccountSettings() {
        UserView loggedUser = PurchaseHistoryApplication.getInstance().getLoggedUser().getValue();
        Preference username = findPreference("username");
        username.setOnPreferenceChangeListener((a, b) -> {
            Log.i(TAG, "loadAccountSettings: username changed: " + b);
            return false;
        });
        Preference password = findPreference("password");
        password.setOnPreferenceChangeListener((a, b) -> {
            Log.i(TAG, "loadAccountSettings: password changed: " + b);
            return false;
        });
        Preference email = findPreference("email");
        email.setOnPreferenceChangeListener((a, b) -> {
            Log.i(TAG, "loadAccountSettings: email changed: " + b);
            return false;
        });
        if (loggedUser != null) {
            username.setSummary(loggedUser.getUsername());
            email.setSummary(loggedUser.getEmail());
        }

    }

    private void loadReferralSettings() {
        Preference referralLink = findPreference("referral_link");
        if (referralLink != null) {
            referralLink.setOnPreferenceClickListener(preference -> {
                new Thread(() -> {
                    Optional<String> token = userClient.getReferralToken();
                    token.ifPresent((t) -> AndroidUtils.shareString(t, "Sharing referral link for your purchase history", requireContext()));
                }).start();
                return true;
            });
        }
    }


}