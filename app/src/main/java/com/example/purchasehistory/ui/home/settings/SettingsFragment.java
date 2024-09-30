package com.example.purchasehistory.ui.home.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.example.purchasehistory.R;

public class SettingsFragment extends PreferenceFragmentCompat {
//    todo: add settings for observability, name and password changes

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}