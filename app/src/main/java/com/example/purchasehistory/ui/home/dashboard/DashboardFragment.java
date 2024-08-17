package com.example.purchasehistory.ui.home.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.purchasehistory.databinding.FragmentDashboardBinding;
import com.example.purchasehistory.ui.home.purchases.PurchasesAdapter;
import com.example.purchasehistory.web.clients.PurchaseClient;
import com.example.purchasehistory.web.clients.UserClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

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
        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}