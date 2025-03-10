package com.angelp.purchasehistory.ui.home.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.FragmentEditProfileBinding;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.UserClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.incoming.UserDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {
    @Inject
    UserClient userClient;
    private FragmentEditProfileBinding binding;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSave;
    private TextView tvFeedback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize views
        etUsername = binding.usernameEdit;
        etEmail = binding.emailEdit;
        etPassword = binding.passwordEdit;
        btnSave = binding.saveButton;
        tvFeedback = new TextView(getContext()); // Add this to your layout if needed

        UserView user = PurchaseHistoryApplication.getInstance().getLoggedUser().getValue(); // Assume this method fetches the user info
        if (user != null) {
            etEmail.setText(user.getEmail());
            etUsername.setText(user.getUsername());
        }

        // Button click listener
        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(username, email, password)) {
                new Thread(() -> attemptProfileUpdate(username, email, password)).start();
            }
        });

        return view;
    }

    private void attemptProfileUpdate(String username, String email, String password) {
        try {
            UserView user = userClient.editUser(new UserDTO(username, email, password));
            new Handler(Looper.getMainLooper()).post(() -> {
                tvFeedback.setText(R.string.profile_updated_successfully);
                tvFeedback.setTextColor(getResources().getColor(R.color.success_green, getContext().getTheme()));
                tvFeedback.setVisibility(View.VISIBLE);
            });
        } catch (WebException e) {
            if (e.getErrorResource().equals(R.string.err1011)) {
                showError(getResources().getString(R.string.invalid_password));
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                tvFeedback.setText(e.getErrorResource());
                tvFeedback.setTextColor(getResources().getColor(R.color.error_red, getContext().getTheme()));
                tvFeedback.setVisibility(View.VISIBLE);
            });
        }
    }

    private boolean validateInputs(String username, String email, String password) {
        if (username.isEmpty()) {
            showError(getResources().getString(R.string.invalid_username));
            return false;
        }
        if (email.isEmpty() || !AndroidUtils.isEmailValid(email)) {
            showError(getResources().getString(R.string.invalid_email));
            return false;
        }
        if (!password.isEmpty() && !AndroidUtils.isPasswordValid(password)) {
            showError(getResources().getString(R.string.invalid_password));
            return false;
        }
        return true;
    }

    private void showError(String message) {
        tvFeedback.setText(message);
        tvFeedback.setTextColor(getResources().getColor(R.color.error_red, getContext().getTheme()));
        tvFeedback.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}