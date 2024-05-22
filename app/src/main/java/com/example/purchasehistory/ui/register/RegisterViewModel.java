package com.example.purchasehistory.ui.register;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.example.purchasehistory.R;
import com.example.purchasehistory.data.model.LoginResult;
import com.example.purchasehistory.web.clients.AuthClient;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;
import java.util.Optional;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> registerResult = new MutableLiveData<>();
    AuthClient authClient;
    @Inject
    public RegisterViewModel(AuthClient authClient) {
        this.authClient = authClient;
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<LoginResult> getRegisterResult() {
        return registerResult;
    }

    public Runnable register(String username, String password, String email) {
        return () -> {
            Optional<UserView> loggedUser = authClient.register(username, password, email);

            if (loggedUser.isPresent()) {
                UserView userView = loggedUser.get();
                registerResult.postValue(new LoginResult(userView));
            } else {
                registerResult.postValue(new LoginResult(R.string.register_failed));
            }
        };
    }

    public Runnable checkIfLoggedIn() {
        return () -> {
            Optional<UserView> loggedUser = authClient.getLoggedUser();
            if (loggedUser.isPresent()) {
                UserView userView = loggedUser.get();
                registerResult.postValue(new LoginResult(userView));
            }
        };
    }

    public void registerDataChanged(String username, String password, String email) {
        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password, null));
        } else if (!isEmailValid(email)) {
            registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_email));
        } else {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder email validation check
    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        } else return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

}