package com.angelp.purchasehistory.ui.home.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.ActivitySettingsBinding;
import com.angelp.purchasehistory.ui.home.HomeActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
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
        if (savedInstanceState == null) {
            String fragmentName = getIntent().getStringExtra("fragment_name");
            if ("MonthlyLimitSettingsFragment".equals(fragmentName)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.settingsContainer.getId(), new MonthlyLimitSettingsFragment())
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(binding.settingsContainer.getId(), SettingsFragment.class, null)
                        .commit();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //Title bar back press triggers onBackPressed()
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i(TAG, "popping backstack");
            fm.popBackStack();
        } else {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}