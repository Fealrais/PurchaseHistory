package com.angelp.purchasehistory.ui.home.dashboard.list;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentPurchasesListCardBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchasesAdapter;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.PurchaseListView;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class PurchaseListDashboardFragment extends RefreshablePurchaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    @Inject
    PurchaseClient purchaseClient;
    private FragmentPurchasesListCardBinding binding;
    private PurchasesAdapter purchasesAdapter;
    private boolean showFilter;
    private int maxSize;

    public PurchaseListDashboardFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        initFilterRow();
        this.applyFilter(filterViewModel.getFilterValue());
        initializePurchasesRecyclerView(maxSize, filterViewModel.getFilterValue());
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchasesListCardBinding.inflate(inflater, container, false);
        maxSize = -1;
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
            maxSize = getArguments().getInt(Constants.Arguments.ARG_MAX_SIZE);
        }
        super.setLoadingScreen(binding.loadingBar);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializePurchasesRecyclerView(int maxSize, PurchaseFilter filter) {
        new Thread(() -> {
            if (binding == null) return;
            PurchaseListView purchaseListView = purchaseClient.getAllPurchases(filter);
            List<PurchaseView> purchases = purchaseListView.getContent();
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity());
            setupShowMoreButton(purchases.size(), maxSize);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding != null) {
                    setShowEmptyView(purchases.isEmpty());
                    binding.purchaseSumText.setText(AndroidUtils.formatCurrency(purchaseListView.getTotalSum(), getContext()));
                    binding.purchaseList.setLayoutManager(llm);
                    binding.purchaseList.setAdapter(purchasesAdapter);
                }
            });
        }).start();
    }

    private void setShowEmptyView(boolean empty) {
        binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void setupShowMoreButton(int purchaseSize, int maxSize) {
        binding.seeAllButton.setOnClickListener((v) -> {
            Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
            DashboardComponent dashboardComponent = new DashboardComponent("PurchaseListPurchaseFragment");
            intent.putExtra(Constants.ARG_COMPONENT, dashboardComponent);
            startActivity(intent);
        });
        updateSeeAllButton(purchaseSize, maxSize);
    }

    private void updateSeeAllButton(int purchaseSize, int maxSize) {
        purchasesAdapter.setLimit(maxSize);
        boolean isBiggerThanLimit = maxSize > 0 && maxSize < purchaseSize;
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.seeAllButton.setText(getString(R.string.see_all_n_purchases, purchaseSize));
            binding.seeAllButton.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
            binding.seeAllBackdrop.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
        });
    }

    public void refresh(PurchaseFilter filter) {
        if (purchasesAdapter == null || binding == null) {
            Log.w(TAG, "refresh: Purchases adapter is missing. Skipping refresh");
            return;
        }
        isRefreshing.postValue(true);
        new Thread(() -> {
            PurchaseListView purchaseListView = purchaseClient.getAllPurchases(filter);
            Log.i(TAG, "Received purchases list with size of " + purchaseListView.getContent().size());
            updateAdapter(purchaseListView);
            isRefreshing.postValue(false);
        }).start();
    }

    private void updateAdapter(PurchaseListView allPurchases) {
        new Handler(Looper.getMainLooper()).post(() -> {
            setShowEmptyView(allPurchases.getContent().isEmpty());
            purchasesAdapter.setPurchaseViews(allPurchases.getContent());
            binding.purchaseSumText.setText(AndroidUtils.formatCurrency(allPurchases.getTotalSum(), getContext()));
            updateSeeAllButton(allPurchases.getContent().size(), maxSize);
        });
    }

    private void initFilterRow() {
        binding.filterButton.setOnClickListener((v) -> openFilter());
        binding.filterDateText.setTextColor(getContext().getColor(R.color.text));
        new Handler(Looper.getMainLooper()).post(() -> binding.filterRow.setVisibility(showFilter ? View.VISIBLE : View.GONE));
    }

    private void applyFilter(PurchaseFilter newFilter) {
        new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null) return;
                    binding.filterDateText.setText(newFilter.getReadableString());
                }
        );
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "barchartFilterDialog");
    }


}