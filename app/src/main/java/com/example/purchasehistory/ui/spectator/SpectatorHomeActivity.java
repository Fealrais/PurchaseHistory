package com.example.purchasehistory.ui.spectator;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.example.purchasehistory.ui.EmptyFragment;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.example.purchasehistory.databinding.ActivitySpectatorBinding;
import com.example.purchasehistory.ui.home.dashboard.DashboardFragment;
import com.example.purchasehistory.web.clients.ObserverClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@AndroidEntryPoint
public class SpectatorHomeActivity extends AppCompatActivity {
    @Inject
    ObserverClient observerClient;
    private ActivitySpectatorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpectatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        new Thread(() -> {
            List<UserView> observedUsers = observerClient.getObservedUsers();
            ArrayAdapter<UserView> observedUserAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, observedUsers);
            binding.spectatorHomeUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    UserView user = observedUsers.get(position);
                    changeObservedUser(user.getId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    changeObservedUser(null);
                }
            });
            runOnUiThread(() -> {
                binding.spectatorHomeUserSpinner.setAdapter(observedUserAdapter);

            });

        }).start();

    }

    private void changeObservedUser(UUID id) {
        if (id != null) {
            PurchaseFilter purchaseFilter = new PurchaseFilter();
            purchaseFilter.setUserId(id);
            if (binding.fragmentContainerView.getFragment() instanceof RefreshablePurchaseFragment) {
                RefreshablePurchaseFragment fragment = binding.fragmentContainerView.getFragment();
                fragment.refresh(purchaseFilter);
            } else {
                DashboardFragment dashboardFragment = new DashboardFragment();
                dashboardFragment.setFilter(purchaseFilter);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(binding.fragmentContainerView.getId(), dashboardFragment)
                        .commit();
            }
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(binding.fragmentContainerView.getId(), new EmptyFragment("User"))
                    .commit();
        }

    }

}