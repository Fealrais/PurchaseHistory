package com.example.purchasehistory.ui.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.example.purchasehistory.R;
import com.example.purchasehistory.databinding.ActivityRegisterBinding;
import com.example.purchasehistory.util.AfterTextChangedWatcher;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private ActivityRegisterBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerViewModel = new ViewModelProvider(this)
                .get(RegisterViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText emailEditText = binding.email;
        final Button registerButton = binding.registerRegisterButton;
        final ProgressBar loadingProgressBar = binding.loading;

        binding.registerBackButton.setOnClickListener((view) -> onBackPressed());

        registerViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null) {
                return;
            }
            registerButton.setEnabled(registerFormState.isDataValid());
            if (registerFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(registerFormState.getUsernameError()));
            }
            if (registerFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(registerFormState.getPasswordError()));
            }
        });

        registerViewModel.getRegisterResult().observe(this, registerResult -> {
            if (registerResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (registerResult.getError() != null) {
                showRegisterFailed(registerResult.getError());
            }
            if (registerResult.getSuccess() != null) {
                updateUiWithUser(registerResult.getSuccess());
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
            finish();
        });
        checkIfLoggedIn();

        TextWatcher afterTextChangedListener = new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), emailEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                register(usernameEditText, passwordEditText, emailEditText);
            }
            return false;
        });

        registerButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            register(usernameEditText, passwordEditText, emailEditText);
        });
    }

    private void checkIfLoggedIn() {
        new Thread(registerViewModel.checkIfLoggedIn(), "CheckLogin").start();
    }

    private void register(EditText usernameEditText, EditText passwordEditText, EditText emailEditText) {
        new Thread(registerViewModel.register(usernameEditText.getText().toString(),
                passwordEditText.getText().toString(), emailEditText.getText().toString()), "Register").start();
    }

    private void updateUiWithUser(UserView ignoredModel) {
        String welcome = getString(R.string.account_created);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegisterFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


}