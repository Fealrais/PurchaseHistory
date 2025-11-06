package com.angelp.purchasehistory.ui.home.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.stream.Collectors;

@NoArgsConstructor
@AndroidEntryPoint
public class DashboardFragment extends RefreshablePurchaseFragment implements CustomizableDashboard {
    private final String TAG = this.getClass().getSimpleName();
    private final Gson gson = new Gson();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;
    private List<DashboardComponent> selectedFragments = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeDashboardFragments();
        filterDialog = new PurchaseFilterDialog(true);
        binding.dashboardFilterButton.setOnClickListener(v -> openFilter());

        binding.customizeDashboardButton.setOnClickListener(v -> openCustomizationDialog());
    }

    private void initializeDashboardFragments() {
        new Thread(() -> {
            List<DashboardComponent> savedFragments = getFragmentsFromPreferences();
            if (savedFragments == null || savedFragments.isEmpty()) {
                savedFragments = new ArrayList<>(Constants.DEFAULT_COMPONENTS);
            } else for (DashboardComponent defaultComponent : Constants.DEFAULT_COMPONENTS) {
                if (!savedFragments.contains(defaultComponent)) {
                    defaultComponent.setVisible(false);
                    savedFragments.add(defaultComponent);
                } // upon application update, the saved fragments might not contain the new default components
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
                .getSharedPreferences(Constants.Preferences.DASHBOARD_PREFS, Context.MODE_PRIVATE);
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

    private void applyFilter(PurchaseFilter newFilter) {
        binding.dashboardFilterButton.setText(R.string.filterButton);
        binding.dashboardFilterDateText.setText(newFilter.getReadableString());
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void refresh(PurchaseFilter filter) {
        if (binding == null) return;
        applyFilter(filter);
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
                DashboardCardFragment dashboardCardFragment = new DashboardCardFragment(selectedFragment);
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
}