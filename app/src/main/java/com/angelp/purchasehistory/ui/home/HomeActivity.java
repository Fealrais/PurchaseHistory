package com.angelp.purchasehistory.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.ActivityHomeBinding;
import com.angelp.purchasehistory.ui.home.settings.SettingsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    @Inject
    AuthClient authClient;
    @Inject
    PurchaseClient purchaseClient;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_qrscanner, R.id.navigation_purchases_list, R.id.navigation_graph)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_user_activity);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
//            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_home_24dp);
        }

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
        } else if (itemId == R.id.menu_item_export_csv) {
            new Thread(() -> {
                Uri exportedCsv = purchaseClient.getExportedCsv();
                Log.i("DOWNLOAD", "Downloaded CSV to " + exportedCsv.getPath());
                PurchaseHistoryApplication.getInstance().alert("Downloaded CSV to " + exportedCsv.getPath());
                openCsvFile(PurchaseHistoryApplication.getContext(), exportedCsv);
            }).start();
        }
        return false;
    }

    private void openCsvFile(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if there's an activity available to handle this intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Handle the case when there's no activity available to handle the intent
            // E.g., show a Toast message
            Toast.makeText(context, "No application available to open CSV files", Toast.LENGTH_SHORT).show();
        }
    }
}