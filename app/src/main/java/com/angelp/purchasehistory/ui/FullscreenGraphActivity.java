package com.angelp.purchasehistory.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.factories.DashboardComponentsFactory;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.ActivityFullscreenGraphBinding;
import com.angelp.purchasehistory.ui.home.HomeActivity;
import com.angelp.purchasehistory.ui.home.dashboard.list.PurchaseListDashboardFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FullscreenGraphActivity extends AppCompatActivity {
    private static final String TAG = FullscreenGraphActivity.class.getSimpleName();
    private ActivityFullscreenGraphBinding binding;
    private DashboardComponent dashboardComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();

        dashboardComponent = getIntent().getParcelableExtra(Constants.ARG_COMPONENT);
        if (dashboardComponent != null) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setIcon(R.drawable.baseline_arrow_back_24);
                actionBar.setTitle(dashboardComponent.getTitle());

            }
            if (dashboardComponent.isLandscapeOnly()) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            RefreshablePurchaseFragment fragment = DashboardComponentsFactory.createFragment(dashboardComponent.getFragmentName());
            if (fragment.getArguments() != null) {
                fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, -1);
                fragment.getArguments().putBoolean(Constants.Arguments.ARG_SHOW_FILTER, true);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .replace(binding.fullscreenFragmentContainer.getId(), fragment);
            if (dashboardComponent.getFragmentName().equals("PieChartFragment")) {
                PurchaseListDashboardFragment listFragment = new PurchaseListDashboardFragment();
                fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, -1);
                fragment.getArguments().putBoolean(Constants.Arguments.ARG_SHOW_FILTER, false);
                transaction.add(binding.secondaryFragmentContainer.getId(), listFragment);
            }
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fullscreen_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //Title bar back press triggers onBackPressed()
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_icon) {
            // Show information about the activity
            new AlertDialog.Builder(this, R.style.BaseDialogStyle)
                    .setTitle(getString(dashboardComponent.getTitle()))
                    .setMessage(getComponentInfo(dashboardComponent))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getComponentInfo(DashboardComponent dashboardComponent) {
        int info = switch (dashboardComponent.getFragmentName()) {
            case "PieChartFragment" -> R.string.help_info_pie_chart;
            case "LineChartFragment" -> R.string.help_info_line_chart;
            case "AccumulativeChartFragment" -> R.string.help_info_accumulative_line_chart;
            case "BarChartFragment" -> R.string.help_info_stacked_bar_chart;
            case "PurchaseListPurchaseFragment" -> R.string.help_info_purchases_list;
            default -> throw new IllegalStateException("Unexpected value: " + dashboardComponent.getFragmentName());
        };
        return getString(info);
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