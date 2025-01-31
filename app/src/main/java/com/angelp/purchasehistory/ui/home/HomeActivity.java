package com.angelp.purchasehistory.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.tour.TourStep;
import com.angelp.purchasehistory.databinding.ActivityHomeBinding;
import com.angelp.purchasehistory.ui.home.settings.SettingsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    private static final String IS_FIRST_TIME_OPEN = "isFirstTimeOpen_HomeActivity";
    public static final String APP_PREFERENCES = "app_preferences";

    @Inject
    AuthClient authClient;
    @Inject
    PurchaseClient purchaseClient;
    private ActivityHomeBinding binding;
    private int tourStep = 0;
    private static final List<TourStep> tourSteps = new ArrayList<>();

    static {
        tourSteps.add(new TourStep(R.id.navigation_dashboard, R.string.tour_navigation_dashboard, R.string.tour_navigation_dashboard_secondary));
        tourSteps.add(new TourStep(R.id.dashboard_filterButton, R.string.tour_filter_button, R.string.tour_filter_button_secondary));
        tourSteps.add(new TourStep(R.id.navigation_qrscanner, R.string.tour_navigation_qrscanner, R.string.tour_navigation_qrscanner_secondary));
        tourSteps.add(new TourStep(R.id.navigation_purchases_list, R.string.tour_navigation_purchases_list, R.string.tour_navigation_purchases_list_secondary));
        tourSteps.add(new TourStep(R.id.navigation_graph, R.string.tour_navigation_graph, R.string.tour_navigation_graph_secondary));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_qrscanner, R.id.navigation_purchases_list, R.id.navigation_graph)
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
    }

    private void showTourPrompt() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.tour_prompt_title)
                .setMessage(R.string.tour_prompt_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> showNextTourStep())
                .setNegativeButton(R.string.skip, (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(IS_FIRST_TIME_OPEN, false);
                    editor.apply();
                })
                .show();
    }

    private boolean isFirstTimeOpen() {
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        return preferences.getBoolean(IS_FIRST_TIME_OPEN, true);
    }

    private void showNextTourStep() {
        if (tourStep < tourSteps.size()) {
            TourStep step = tourSteps.get(tourStep);
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
            SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(IS_FIRST_TIME_OPEN, false);
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