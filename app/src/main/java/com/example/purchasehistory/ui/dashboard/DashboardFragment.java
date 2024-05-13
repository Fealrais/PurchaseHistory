package com.example.purchasehistory.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.databinding.FragmentDashboardBinding;
import com.example.purchasehistory.web.clients.PurchaseClient;
import com.example.purchasehistory.web.clients.UserClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    @Inject
    UserClient userClient;
    @Inject
    PurchaseClient purchaseClient;
    private PurchasesAdapter purchasesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.swiperefresh.setOnRefreshListener(this::onSwipeRefresh);
        initializePurchasesRecyclerView();
        return root;
    }

    private void initializePurchasesRecyclerView() {
        new Thread(() -> {
            List<PurchaseView> purchases = dashboardViewModel.getAllPurchases();
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity());
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            getActivity().runOnUiThread(() -> {
                binding.purchases.setLayoutManager(llm);
                binding.purchases.setItemAnimator(new DefaultItemAnimator());
                binding.purchases.setAdapter(purchasesAdapter);
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onSwipeRefresh() {
        Log.i(TAG, "onRefresh called");
        new Thread(() -> {
            purchasesAdapter.getPurchaseViews().clear();
            List<PurchaseView> allPurchases = dashboardViewModel.getAllPurchases();
            Log.i(TAG, "Received purchases list with size of " + allPurchases.size());
            purchasesAdapter.getPurchaseViews().addAll(allPurchases);
            getActivity().runOnUiThread(() -> purchasesAdapter.notifyDataSetChanged());
            Log.i(TAG, "Adapter notified");
            binding.swiperefresh.setRefreshing(false);
        }).start();
    }
}