package com.angelp.purchasehistory.ui.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.ActivityRegisterBinding;
import com.angelp.purchasehistory.ui.legal.PrivacyPolicyActivity;
import com.angelp.purchasehistory.ui.legal.TermsAndConditionsActivity;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.Locale;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private ActivityRegisterBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText confirmPassword = binding.confirmPassword;
        final EditText emailEditText = binding.email;
        final Button registerButton = binding.registerRegisterButton;
        final ProgressBar loadingProgressBar = binding.loading;
        final CheckBox acceptTermsCheckBox = binding.acceptTerms;

        binding.registerBackButton.setOnClickListener((view) -> onBackPressed());

        registerViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null) {
                return;
            }
            registerButton.setEnabled(registerFormState.isDataValid() && acceptTermsCheckBox.isChecked());
            if (registerFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(registerFormState.getUsernameError()));
            }
            if (registerFormState.getPasswordError() != null) {
                if (registerFormState.getPasswordError().equals(R.string.invalid_password_match))
                    confirmPassword.setError(getString(registerFormState.getPasswordError()));
                else
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
            finish();
        });

        checkIfLoggedIn();

        TextWatcher afterTextChangedListener = new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(usernameEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim(), confirmPassword.getText().toString().trim(), emailEditText.getText().toString().trim());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPassword.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);

        acceptTermsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean dataValid = registerViewModel.getRegisterFormState().getValue() != null && registerViewModel.getRegisterFormState().getValue().isDataValid();
            registerButton.setEnabled(dataValid && isChecked);
        });

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
        binding.termsLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, TermsAndConditionsActivity.class);
            startActivity(intent);
        });

        binding.privacyLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }

    private void checkIfLoggedIn() {
        new Thread(registerViewModel.checkIfLoggedIn(), "CheckLogin").start();
    }

    private void register(EditText usernameEditText, EditText passwordEditText, EditText emailEditText) {
        LocaleList locales = getResources().getConfiguration().getLocales();
        Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);

        new Thread(registerViewModel.register(usernameEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim(), emailEditText.getText().toString().trim(), locale), "Register").start();
    }

    private void updateUiWithUser(UserView ignoredModel) {
        String welcome = getString(R.string.account_created);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegisterFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


}