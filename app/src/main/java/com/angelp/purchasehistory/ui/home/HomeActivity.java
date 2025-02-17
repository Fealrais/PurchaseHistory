package com.angelp.purchasehistory.ui.home;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.tour.TourStep;
import com.angelp.purchasehistory.databinding.ActivityHomeBinding;
import com.angelp.purchasehistory.receivers.AppBootReceiver;
import com.angelp.purchasehistory.ui.home.settings.SettingsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import javax.inject.Inject;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    @Inject
    AuthClient authClient;
    @Inject
    PurchaseClient purchaseClient;
    private ActivityHomeBinding binding;
    private int tourStep = 0;

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_qrscanner, R.id.navigation_scheduled_expenses, R.id.navigation_profile)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_user_activity);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_home_24dp);

        }
        if (isFirstTimeOpen()) {
            showTourPrompt();
        }
        Intent intent = new Intent(this, AppBootReceiver.class);
        intent.setAction(Constants.APP_BOOT_RECEIVER);
        sendBroadcast(intent);
    }

    private void showTourPrompt() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.tour_guide_bubble);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button yesButton = dialog.findViewById(R.id.tour_guide_next);
        yesButton.setOnClickListener(v -> {
            binding.navView.setEnabled(false);
            findViewById(R.id.dashboard_filterButton).setEnabled(false);
            showNextTourStep();
            dialog.dismiss();
        });

        Button skipButton = dialog.findViewById(R.id.tour_guide_skip);
        skipButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(Constants.Preferences.APP_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.Preferences.IS_FIRST_TIME_OPEN, false);
            editor.apply();
            dialog.dismiss();
        });
        dialog.show();
    }
    private boolean isFirstTimeOpen() {
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        return preferences.getBoolean(Constants.Preferences.IS_FIRST_TIME_OPEN, true);
    }

    private void showNextTourStep() {
        if (tourStep < Constants.tourSteps.size()) {
            TourStep step = Constants.tourSteps.get(tourStep);
            new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(findViewById(step.getId()))
                    .setPrimaryText(step.getPrimaryText())
                    .setSecondaryText(step.getSecondaryText())
                    .setPromptStateChangeListener((prompt, state) -> {
                        if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                            tourStep++;
                            showNextTourStep();
                        }
                    })
                    .show();
        } else {
            binding.navView.setEnabled(true);
            SharedPreferences preferences = getSharedPreferences(Constants.Preferences.APP_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.Preferences.IS_FIRST_TIME_OPEN, false);
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.menu_item_logout) {
            new Thread(() -> {
                authClient.logout();
                AndroidUtils.logout(this);
            }).start();
        }
        return false;
    }
}