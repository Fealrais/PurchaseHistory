package com.angelp.purchasehistory.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.data.tour.TourStep;
import com.angelp.purchasehistory.databinding.ActivityHomeBinding;
import com.angelp.purchasehistory.ui.home.dashboard.CustomizationDialogFragment;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NonNull;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    @Inject
    AuthClient authClient;
    @Inject
    PurchaseClient purchaseClient;
    private ActivityHomeBinding binding;
    private int tourStep = 0;
    private NavController navController;
    private final Gson gson = new Gson();
    private Menu menu;

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
        ActionBar actionBar = getSupportActionBar();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_user_activity);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            navController.addOnDestinationChangedListener((controller, currentDest, c) -> {
                if (menu!=null){
                    MenuItem item = menu.findItem(R.id.menu_dashboard_settings);
                    if (item != null) item.setVisible(Objects.equals(currentDest.getId(), R.id.navigation_dashboard));
                }
            });
            NavigationUI.setupWithNavController(binding.navView, navController);
            NavigationUI.setupActionBarWithNavController(this, navHostFragment.getNavController(), appBarConfiguration);
        }
        if (isFirstTimeOpen()) {
            showTourPrompt();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void showTourPrompt() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.tour_guide_bubble);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button yesButton = dialog.findViewById(R.id.tour_guide_next);
        yesButton.setOnClickListener(v -> {
            binding.navView.setEnabled(false);
            findViewById(R.id.filter_btn).setEnabled(false);
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
        getMenuInflater().inflate(R.menu.home_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private String getCurrentHelpPrompt(NavDestination currentDestination) {
        int id = currentDestination.getId();
        if (Objects.equals(id, R.id.navigation_dashboard))
            return getString(R.string.help_dashboard);
        if (Objects.equals(id, R.id.navigation_qrscanner))
            return getString(R.string.help_schedule);
        if (Objects.equals(id, R.id.navigation_profile))
            return getString(R.string.help_qrscanner);
        if (Objects.equals(id, R.id.navigation_scheduled_expenses))
            return getString(R.string.help_profile);
        return "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.menu_dashboard_settings) {
            openCustomizationDialog();
        } else if (itemId == R.id.menu_help && navController != null) {
            showHelp(navController.getCurrentDestination());
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelp(NavDestination currentDestination) {
        String currentHelpPrompt = getCurrentHelpPrompt(currentDestination);
        new AlertDialog.Builder(this, R.style.BaseDialogStyle)
                .setMessage(currentHelpPrompt)
                .create().show();
    }

    private void openCustomizationDialog() {
        List<DashboardComponent> fragments = getFragmentsFromPreferences();
        CustomizationDialogFragment dialog = new CustomizationDialogFragment(fragments, updatedFragments -> {

            saveFragmentsSetupToPreferences(updatedFragments);
            Fragment dashboard = binding.navHostFragmentUserActivity.getFragment();
            if (dashboard != null) {
                getSupportFragmentManager().beginTransaction().detach(dashboard).commit();
                getSupportFragmentManager().beginTransaction().attach(dashboard).commit();
            }
        });
        dialog.show(getSupportFragmentManager(), "customizationDialog");
    }

    private List<DashboardComponent> getFragmentsFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE);
        String savedFragmentsJson = preferences.getString("saved_fragments", "[]");
        Type type = new TypeToken<List<DashboardComponent>>() {
        }.getType();

        List<DashboardComponent> dashboardComponents = gson.fromJson(savedFragmentsJson, type);
        return dashboardComponents.stream().map(DashboardComponent::fillFromName).collect(Collectors.toList());
    }

    private void saveFragmentsSetupToPreferences(List<DashboardComponent> selectedFragments) {
        SharedPreferences preferences = PurchaseHistoryApplication.getContext()
                .getSharedPreferences(Constants.Preferences.DASHBOARD_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String fragmentsJson = gson.toJson(selectedFragments);
        editor.remove("saved_fragments");
        editor.putString("saved_fragments", fragmentsJson);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndroidUtils.SAVE_CSV_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                new Thread(() -> purchaseClient.getExportedCsv(this, uri)).start();
            }
        }
    }
}