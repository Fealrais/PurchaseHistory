package com.angelp.purchasehistory.ui.home.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.FragmentChangePasswordBinding;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.UserClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.incoming.UpdatePasswordDTO;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    @Inject
    UserClient userClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.saveButton.setOnClickListener(v -> {
            String newPassword = binding.newPassword.getText().toString().trim();
            String newPasswordConfirm = binding.newPasswordConfirm.getText().toString().trim();
            String currentPassword = binding.currentPasswordEdit.getText().toString().trim();

            if (validateInputs(newPassword, newPasswordConfirm, currentPassword)) {
                new Thread(() -> attemptPasswordChange(newPassword, currentPassword)).start();
            }
        });

        return view;
    }

    private void attemptPasswordChange(String newPassword, String oldPassword) {
        try {
            Log.d("ChangePasswordFragment", "Attempting to change password");
            userClient.updatePassword(new UpdatePasswordDTO(oldPassword, newPassword));
            Log.i("ChangePasswordFragment", "Password changed successfully");
            AndroidUtils.showSuccessAnimation(getView());
            new Handler(Looper.getMainLooper()).postDelayed(() -> getParentFragmentManager().popBackStack(), 1500);
        } catch (WebException e) {
            Log.e("ChangePasswordFragment", "Password change failed: " + e.getMessage());
            showError(binding.currentPasswordEdit, getResources().getString(e.getErrorResource()));
        }

    }

    private boolean validateInputs(String newPassword, String newPasswordConfirm, String oldPassword) {
        if (!newPassword.isEmpty() && !AndroidUtils.isPasswordValid(newPassword)) {
            showError(binding.newPassword, getResources().getString(R.string.invalid_password));
            return false;
        }
        if (!newPasswordConfirm.isEmpty() && !AndroidUtils.isPasswordValid(newPasswordConfirm)) {
            showError(binding.newPasswordConfirm, getResources().getString(R.string.invalid_password));
            return false;
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            showError(binding.newPasswordConfirm, getResources().getString(R.string.invalid_password_match));
            return false;
        }
        if (!oldPassword.isEmpty() && !AndroidUtils.isPasswordValid(oldPassword)) {
            showError(binding.currentPasswordEdit, getResources().getString(R.string.invalid_password));
            return false;
        }
        return true;
    }

    private void showError(EditText edit, String message) {
        new Handler(Looper.getMainLooper()).post(() -> edit.setError(message));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}