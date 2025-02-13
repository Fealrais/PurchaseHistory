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
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentDashboardBinding;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    public static final String DASHBOARD_FRAGMENT = "dashboardFragment";
    private final String DASHBOARD_FILTER = "dashboard_filter";
    private final String TAG = this.getClass().getSimpleName();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;
    private List<DashboardComponent> selectedFragments = new ArrayList<>();
    @Setter
    private PurchaseFilter filter;
    private final Gson gson = new Gson();;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        selectedFragments = initializeDashboardFragments();

        filterDialog = new PurchaseFilterDialog(false);
        this.applyFilter(filter);
        binding.dashboardFilterButton.setOnClickListener(v -> openFilter(this::updateFilter));
        binding.customizeDashboardButton.setOnClickListener(v -> openCustomizationDialog());
        return binding.getRoot();
    }

    private List<DashboardComponent> initializeDashboardFragments() {
        List<DashboardComponent> savedFragments = getFragmentsFromPreferences();
        if (savedFragments == null || savedFragments.isEmpty()) {
            savedFragments = new ArrayList<>(CustomizationAdapter.DEFAULT_COMPONENTS);
        }
        setupFragments(new ArrayList<>(), savedFragments);
        return savedFragments;
    }

    private List<DashboardComponent> getFragmentsFromPreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE);
        String savedFragmentsJson = preferences.getString("saved_fragments", "[]");
        Type type = new TypeToken<List<DashboardComponent>>() {
        }.getType();

        List<DashboardComponent> dashboardComponents = gson.fromJson(savedFragmentsJson, type);
        return dashboardComponents.stream().map(DashboardComponent::fillFromName).collect(Collectors.toList());
    }

    private void saveFragmentsSetupToPreferences(List<DashboardComponent> selectedFragments) {
        SharedPreferences preferences = getActivity().getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE);
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
        if (bundle == null || bundle.getParcelable(DASHBOARD_FILTER) == null)
            return getDefaultFilter();
        return bundle.getParcelable(DASHBOARD_FILTER);
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.dashboardFilterButton.setText(R.string.filterButton);
        binding.dashboardFilterDateText.setText(filter.getReadableString());
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
        outState.putParcelable(DASHBOARD_FILTER, filter);
    }

    public void refresh(PurchaseFilter filter) {
        for (int i = 0; i < binding.dashboardFragmentsLinearLayout.getChildCount(); i++) {
            Fragment fragment = getParentFragmentManager().findFragmentByTag(DASHBOARD_FRAGMENT + i);
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
                    DashboardCardFragment dashboardCardFragment = new DashboardCardFragment(selectedFragment, filter, this::setFilter);
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
        this.filter = newFilter;
        this.applyFilter(newFilter);
        if (filterDialog.isAdded())
            filterDialog.dismiss();
        refresh(newFilter);
    }
}