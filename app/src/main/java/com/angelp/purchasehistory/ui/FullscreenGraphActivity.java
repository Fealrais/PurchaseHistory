package com.angelp.purchasehistory.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.factories.DashboardComponentsFactory;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.ActivityFullscreenGraphBinding;
import com.angelp.purchasehistory.ui.home.HomeActivity;
import com.angelp.purchasehistory.ui.home.dashboard.list.PurchaseListDashboardFragment;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class FullscreenGraphActivity extends AppCompatActivity {
    private static final String TAG = FullscreenGraphActivity.class.getSimpleName();
    private ActivityFullscreenGraphBinding binding;
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    private DashboardComponent dashboardComponent;
    @Inject
    protected PurchaseFilterSingleton filterViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();

        dashboardComponent = getIntent().getParcelableExtra(Constants.Arguments.ARG_COMPONENT);
        if (dashboardComponent != null) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setIcon(R.drawable.arrow_turn_left);
                actionBar.setTitle(dashboardComponent.getTitle());

            }
            RefreshablePurchaseFragment fragment = DashboardComponentsFactory.createFragment(dashboardComponent.getFragmentName());

            if (dashboardComponent.isLandscapeOnly()) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fragment.getArguments().putInt(Constants.Arguments.EXTERNAL_LEGEND, R.id.legendList);
            }

            binding.verticalFilterBar.filterBtn.setOnClickListener(v -> openFilter());
            binding.filterBar.filterBtn.setOnClickListener(v -> openFilter());

            applyFilter(filterDialog.getFilter());
            filterViewModel.getFilter().observe(this, this::applyFilter);

            if (fragment.getArguments() != null) {
                fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, -1);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .replace(binding.fullscreenFragmentContainer.getId(), fragment);
            if (dashboardComponent.getFragmentName().equals("PieChartFragment") && binding.secondaryFragmentContainer != null) {
                binding.secondaryFragmentContainer.setVisibility(View.VISIBLE);
                PurchaseListDashboardFragment listFragment = new PurchaseListDashboardFragment();
                fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, -1);
                transaction.replace(binding.secondaryFragmentContainer.getId(), listFragment);
            } else {
                binding.secondaryFragmentContainer.setVisibility(View.GONE);
            }
            transaction.commit();
        }
    }

    private void openFilter() {
        filterDialog.show(getSupportFragmentManager(), "chart_filter");
    }

    private void applyFilter(PurchaseFilter newFilter) {
        if (binding == null || newFilter == null) return;

        int color = newFilter.getCategoryId() == null ? getResources().getColor(R.color.surfaceA20) : AndroidUtils.getColor(newFilter.getCategoryColor());
        binding.filterBar.filterCategoryBtn.getBackground().setTint(color);
        binding.filterBar.filterCategoryBtn.setTextColor(AndroidUtils.getTextColor(color));
        binding.filterBar.filterCategoryBtn.setText(newFilter.getCategoryName() == null ? getString(R.string.category) : newFilter.getCategoryName());
        binding.filterBar.filterDateBtn.setText(newFilter.getDateString());
        binding.verticalFilterBar.filterCategoryBtn.getBackground().setTint(color);
        binding.verticalFilterBar.filterCategoryBtn.setTextColor(AndroidUtils.getTextColor(color));
        binding.verticalFilterBar.filterCategoryBtn.setText(newFilter.getCategoryName() == null ? getString(R.string.category) : newFilter.getCategoryName());
        binding.verticalFilterBar.filterDateBtn.setText(newFilter.getDateString());
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