package com.example.purchasehistory.ui.spectator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.example.purchasehistory.R;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.example.purchasehistory.databinding.ActivitySpectatorBinding;
import com.example.purchasehistory.ui.EmptyFragment;
import com.example.purchasehistory.ui.home.dashboard.DashboardFragment;
import com.example.purchasehistory.ui.home.settings.SettingsActivity;
import com.example.purchasehistory.util.AndroidUtils;
import com.example.purchasehistory.web.clients.AuthClient;
import com.example.purchasehistory.web.clients.ObserverClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@AndroidEntryPoint
public class SpectatorHomeActivity extends AppCompatActivity {
    @Inject
    ObserverClient observerClient;
    @Inject
    AuthClient authClient;
    private ActivitySpectatorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpectatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.menu_item_logout) {
            new Thread(() -> {
                authClient.logout();
                AndroidUtils.logout(this);
            }).start();
        }
        return false;
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