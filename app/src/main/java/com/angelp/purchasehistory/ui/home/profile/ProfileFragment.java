package com.angelp.purchasehistory.ui.home.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.FragmentProfileBinding;
import com.angelp.purchasehistory.ui.home.settings.SettingsActivity;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.UserClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.Optional;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    @Inject
    UserClient userClient;
    @Inject
    PurchaseClient purchaseClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Set up user info preview
        UserView user = PurchaseHistoryApplication.getInstance().getLoggedUser().getValue(); // Assume this method fetches the user info
        if(user!=null) {
            binding.username.setText(getString(R.string.username_param, user.getUsername()));
            binding.email.setText(getString(R.string.email_param, user.getEmail()));
        }

        // Set up button click listeners
        binding.editButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_navigation_edit_profile)
        );
        // Set up button click listeners
        binding.btnChangePassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_navigation_change_password)
        );

        binding.downloadSvgButton.setOnClickListener(v -> downloadUserData());
        binding.shareLinkButton.setOnClickListener(v -> shareToken());
        binding.logoutButton.setOnClickListener(v -> AndroidUtils.logout(v.getContext()));

        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        binding.deleteAccountButton.setOnClickListener(v -> showDeleteAccountConfirmation());

        return binding.getRoot();
    }

    private void shareToken() {
        new Thread(() -> {
            Optional<String> token = userClient.getReferralToken();
            token.ifPresent((t) -> AndroidUtils.shareString(t, "Sharing referral link for your purchase history", requireContext()));
        }).start();
    }


    private void downloadUserData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "purchase_history_data.csv");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, AndroidUtils.SAVE_CSV_REQUEST_CODE);
    }
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_description)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    new Thread(() -> {
                        boolean b = userClient.deleteAccount();
                        if (b) AndroidUtils.logout(getContext());
                    }).start();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}