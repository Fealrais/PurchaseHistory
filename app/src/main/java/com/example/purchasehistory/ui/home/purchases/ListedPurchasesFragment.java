package com.example.purchasehistory.ui.home.purchases;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.databinding.FragmentListedPurchasesBinding;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@AndroidEntryPoint
public class ListedPurchasesFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentListedPurchasesBinding binding;
    private PurchasesViewModel purchasesViewModel;
    private PurchasesAdapter purchasesAdapter;
    private PurchaseFilterDialog filterDialog;
    @Setter
    private PurchaseFilter filter = new PurchaseFilter();

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        purchasesViewModel = new ViewModelProvider(this).get(PurchasesViewModel.class);
        initializePurchasesRecyclerView();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListedPurchasesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void initializePurchasesRecyclerView() {
        filterDialog = new PurchaseFilterDialog();
        binding.swiperefresh.setOnRefreshListener(this::onSwipeRefresh);
        binding.filterButton.setOnClickListener(v -> openFilter((newFilter) -> {
            setFilter(newFilter);
            binding.filterButton.setText(newFilter.isEmpty() ? "Filter" : "Filtered");
            onSwipeRefresh();
        }));
        new Thread(() -> {
            List<PurchaseView> purchases = purchasesViewModel.getPurchaseClient().getAllPurchases(filter);
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity());
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            getActivity().runOnUiThread(() -> {
                binding.purchaseList.setLayoutManager(llm);
                binding.purchaseList.setItemAnimator(new DefaultItemAnimator());
                binding.purchaseList.setAdapter(purchasesAdapter);
            });
        }).start();
    }

    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(new PurchaseFilter());
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
        filterDialog.setOnSuccess(setFilter);
    }

    private void onSwipeRefresh() {
        new Thread(() -> {
            purchasesAdapter.getPurchaseViews().clear();
            List<PurchaseView> allPurchases = purchasesViewModel.getPurchaseClient().getAllPurchases(filter);
            Log.i(TAG, "Received purchases list with size of " + allPurchases.size());
            purchasesAdapter.setPurchaseViews(allPurchases);
            getActivity().runOnUiThread(() -> purchasesAdapter.notifyDataSetChanged());
            Log.i(TAG, "Adapter notified");
            binding.swiperefresh.setRefreshing(false);
        }).start();
    }


}