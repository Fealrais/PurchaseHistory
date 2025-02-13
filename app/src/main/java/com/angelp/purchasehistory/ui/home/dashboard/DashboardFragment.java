package com.angelp.purchasehistory.ui.home.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentDashboardBinding;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@NoArgsConstructor
@AndroidEntryPoint
public class DashboardFragment extends Fragment implements RefreshablePurchaseFragment, CustomizableDashboard {
    private final String TAG = this.getClass().getSimpleName();
    private final Gson gson = new Gson();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;
    private List<DashboardComponent> selectedFragments = new ArrayList<>();
    private PurchaseFilter filter = Constants.getDefaultFilter();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        initializeDashboardFragments();
        initializeFromArgs(savedInstanceState);
        filterDialog = new PurchaseFilterDialog(false);
        this.applyFilter(filter);
        binding.dashboardFilterButton.setOnClickListener(v -> openFilter(this::updateFilter));
        binding.customizeDashboardButton.setOnClickListener(v -> openCustomizationDialog());
        return binding.getRoot();
    }

    private void initializeFromArgs(Bundle state) {
        if (state == null) return;
        PurchaseFilter purchaseFilter = state.getParcelable(Constants.DASHBOARD_FILTER);
        if (purchaseFilter != null) filter = purchaseFilter;
    }

    private void initializeDashboardFragments() {
        new Thread(() -> {
            List<DashboardComponent> savedFragments = getFragmentsFromPreferences();
            if (savedFragments == null || savedFragments.isEmpty()) {
                savedFragments = new ArrayList<>(Constants.DEFAULT_COMPONENTS);
            }
            setupFragments(new ArrayList<>(), savedFragments);
            selectedFragments = savedFragments;
        }).start();
    }

    private List<DashboardComponent> getFragmentsFromPreferences() {
        if (getActivity() == null) return null;
        SharedPreferences preferences = getActivity().getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE);
        String savedFragmentsJson = preferences.getString("saved_fragments", "[]");
        Type type = new TypeToken<List<DashboardComponent>>() {
        }.getType();

        List<DashboardComponent> dashboardComponents = gson.fromJson(savedFragmentsJson, type);
        return dashboardComponents.stream().map(DashboardComponent::fillFromName).collect(Collectors.toList());
    }

    private void saveFragmentsSetupToPreferences(List<DashboardComponent> selectedFragments) {
        SharedPreferences preferences = PurchaseHistoryApplication.getContext()
                .getSharedPreferences(Constants.DASHBOARD_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String fragmentsJson = gson.toJson(selectedFragments);
        editor.remove("saved_fragments");
        editor.putString("saved_fragments", fragmentsJson);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!binding.dashboardFilterButton.isEnabled()) binding.dashboardFilterButton.setEnabled(true);
    }

    private PurchaseFilter loadFilterArg(Bundle bundle) {
        if (bundle == null || bundle.getParcelable(Constants.DASHBOARD_FILTER) == null)
            return getDefaultFilter();
        return bundle.getParcelable(Constants.DASHBOARD_FILTER);
    }

    private void applyFilter(PurchaseFilter newFilter) {
        this.filter = newFilter;
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.dashboardFilterButton.setText(R.string.filterButton);
            binding.dashboardFilterDateText.setText(newFilter.getReadableString());
        });
    }

    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(getDefaultFilter());
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
        filterDialog.setOnSuccess(setFilter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.filter = loadFilterArg(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.DASHBOARD_FILTER, filter);
    }

    public void refresh(PurchaseFilter filter) {
        for (int i = 0; i < binding.dashboardFragmentsLinearLayout.getChildCount(); i++) {
            Fragment fragment = getParentFragmentManager().findFragmentByTag(Constants.DASHBOARD_FRAGMENT + i);
            refreshFragment(filter, fragment);
        }
    }

    private void refreshFragment(PurchaseFilter filter, Fragment fragment) {
        if (fragment instanceof RefreshablePurchaseFragment refreshablePurchaseFragment) {
            refreshablePurchaseFragment.refresh(filter);
        }
    }

    private void setupFragments(List<DashboardComponent> fragments, List<DashboardComponent> newFragments) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            Fragment fragment = getParentFragmentManager().findFragmentByTag("dashboardFragment" + i);
            if (fragment != null)
                transaction.remove(fragment);
        }
        for (int i = 0; i < newFragments.size(); i++) {
            DashboardComponent selectedFragment = newFragments.get(i);
            if (selectedFragment.isVisible()) {
                DashboardCardFragment dashboardCardFragment = new DashboardCardFragment(selectedFragment, filter, this::updateFilter);
                transaction.add(binding.dashboardFragmentsLinearLayout.getId(), dashboardCardFragment, "dashboardFragment" + i);
            }
        }
        transaction.commit();
    }

    private void openCustomizationDialog() {
        CustomizationDialogFragment dialog = new CustomizationDialogFragment(selectedFragments, updatedFragments -> {

            saveFragmentsSetupToPreferences(updatedFragments);
            setupFragments(selectedFragments, updatedFragments);
            selectedFragments.clear();
            selectedFragments.addAll(updatedFragments);
        });
        dialog.show(getParentFragmentManager(), "customizationDialog");
    }

    private void updateFilter(PurchaseFilter newFilter) {
        new Handler(Looper.getMainLooper()).post(() -> {
            this.applyFilter(newFilter);
            if (filterDialog.isAdded())
                filterDialog.dismiss();
            refresh(newFilter);
        });
    }
}