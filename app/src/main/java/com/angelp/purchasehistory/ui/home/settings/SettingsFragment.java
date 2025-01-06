package com.angelp.purchasehistory.ui.home.settings;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.UserClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.incoming.UserDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat {
    private final String TAG = getClass().getName();
    @Inject
    UserClient userClient;
    @Inject
    PurchaseClient purchaseClient;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
        loadAccountSettings();
        loadReferralSettings();
    }

    private void loadAccountSettings() {
        UserView loggedUser = PurchaseHistoryApplication.getInstance().getLoggedUser().getValue();
        Preference username = findPreference("username");
        username.setOnPreferenceChangeListener((prev, nextValue) -> {
            Log.i(TAG, "loadAccountSettings: username " + prev + " changed: " + nextValue);
            if (AndroidUtils.isUserNameValid((String) nextValue)) {
                updateUser(new UserDTO((String) nextValue, null, null, null));
                return true;
            }
            PurchaseHistoryApplication.getInstance().getApplicationContext().getMainExecutor().execute(
                    () -> Toast.makeText(getContext(), R.string.invalid_username, Toast.LENGTH_SHORT).show());
            return false;
        });
        Preference password = findPreference("password");
        password.setOnPreferenceChangeListener((prev, nextValue) -> {
            Log.i(TAG, "loadAccountSettings: password changed: " + nextValue);
            if (AndroidUtils.isPasswordValid((String) nextValue)) {
                updateUser(new UserDTO(null, (String) nextValue, null, null));
                return true;
            }
            PurchaseHistoryApplication.getInstance().getApplicationContext().getMainExecutor().execute(
                    () -> Toast.makeText(getContext(), R.string.invalid_password, Toast.LENGTH_SHORT).show());
            return false;
        });
        Preference email = findPreference("email");
        email.setOnPreferenceChangeListener((prev, nextValue) -> {
            Log.i(TAG, "loadAccountSettings: email changed: " + nextValue);
            if (AndroidUtils.isEmailValid((String) nextValue)) {
                updateUser(new UserDTO(null, null, (String) nextValue, null));
                return true;
            }
            PurchaseHistoryApplication.getInstance().getApplicationContext().getMainExecutor().execute(
                    () -> Toast.makeText(getContext(), R.string.invalid_email, Toast.LENGTH_SHORT).show());
            return false;
        });
        Preference downloadCsv = findPreference("download_svg");
        if (downloadCsv != null) {
            downloadCsv.setOnPreferenceClickListener(preference -> {
                new Thread(() -> {
                    try {
                        Uri exportedCsv = purchaseClient.getExportedCsv();
                        Log.i("DOWNLOAD", "Downloaded CSV to " + exportedCsv.getPath());
                        PurchaseHistoryApplication.getInstance().alert("Downloaded CSV to " + exportedCsv.getPath());
                        AndroidUtils.openCsvFile(PurchaseHistoryApplication.getContext(), exportedCsv);
                    } catch (WebException e) {
                        Log.e(TAG, "loadAccountSettings:"+e.getMessage() );
                        e.printStackTrace();
                        PurchaseHistoryApplication.getInstance().getApplicationContext().getMainExecutor().execute(
                                () -> Toast.makeText(PurchaseHistoryApplication.getContext(), e.getErrorResource(), Toast.LENGTH_SHORT).show());
                    } catch (IOException e) {
                        Log.e(TAG, "loadAccountSettings:"+e.getMessage() );
                    }
                }).start();
                return true;
            });
        }
        updateUi(loggedUser);

    }

    private void updateUser(UserDTO user) {
        new Thread(() -> {
            UserView newUser = userClient.editUser(user);
            new Handler(Looper.getMainLooper()).post(() -> {
                updateUi(newUser);
            });
        }
        ).start();
    }

    private void updateUi(UserView newUser) {
        if (newUser != null) {
            Preference username = findPreference("username");
            Preference email = findPreference("email");
            username.setSummary(newUser.getUsername());
            email.setSummary(newUser.getEmail());
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