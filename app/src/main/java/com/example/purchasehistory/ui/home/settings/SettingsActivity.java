package com.example.purchasehistory.ui.home.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.purchasehistory.R;
import com.example.purchasehistory.databinding.ActivitySettingsBinding;
import com.example.purchasehistory.ui.home.HomeActivity;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.baseline_arrow_back_24);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.settingsContainer.getId(), SettingsFragment.class, null)
                .commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        return true;
    }
}