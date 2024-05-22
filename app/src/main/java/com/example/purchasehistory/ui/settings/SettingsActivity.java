package com.example.purchasehistory.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.purchasehistory.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        getSupportFragmentManager()
                .beginTransaction()
                .add(binding.settingsContainer.getId(), new SettingsFragment())
                .commit();
    }
}