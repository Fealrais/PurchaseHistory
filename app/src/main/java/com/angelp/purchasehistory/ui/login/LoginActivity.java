package com.angelp.purchasehistory.ui.login;

import android.app.Activity;
import android.content.Intent;
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
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.databinding.ActivityLoginBinding;
import com.angelp.purchasehistory.receivers.AppBootReceiver;
import com.angelp.purchasehistory.ui.forgotpassword.ForgotPasswordEmailActivity;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.angelp.purchasehistory.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);
        checkIfLoggedIn();

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.loginLoginButton;
        final Button forgotPasswordButton = binding.forgotPasswordButton;
        final ProgressBar loadingProgressBar = binding.loading;

        binding.backButton.setOnClickListener((view) -> onBackPressed());

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            loadingProgressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            if (loginResult.getSuccess() == null) {
                showLoginFailed(loginResult.getError());
            } else {
                Intent intent = new Intent(this, AppBootReceiver.class);
                intent.setAction(Constants.APP_BOOT_RECEIVER);
                sendBroadcast(intent);
                updateUiWithUser(loginResult.getSuccess().getUsername());
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin(usernameEditText, passwordEditText);
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            attemptLogin(usernameEditText, passwordEditText);
        });
        forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordEmailActivity.class);
            startActivity(intent);
        });
//        binding.googleSignInButton.setOnClickListener((v)-> loginWithGoogle());
    }

    private void checkIfLoggedIn() {
        new Thread(loginViewModel.checkIfLoggedIn(), "CheckLogin").start();
    }

    private void attemptLogin(EditText usernameEditText, EditText passwordEditText) {
        new Thread(loginViewModel.login(usernameEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim()), "Login").start();
    }

    private void updateUiWithUser(String username) {
        String welcome = String.format(getString(R.string.welcome), username);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


    private void loginWithGoogle() {


    }

}