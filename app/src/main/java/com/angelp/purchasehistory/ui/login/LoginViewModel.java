package com.angelp.purchasehistory.ui.login;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.model.LoginResult;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;
import java.util.Optional;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final AuthClient authClient;

    @Inject
    public LoginViewModel(AuthClient authClient) {
        this.authClient = authClient;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public Runnable login(String username, String password) {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.login(username, password);

                if (loggedUser.isPresent()) {
                    UserView userView = loggedUser.get();
                    PurchaseHistoryApplication.getInstance().loggedUser.postValue(userView);
                    loginResult.postValue(new LoginResult(userView));
                } else {
                    loginResult.postValue(new LoginResult(R.string.login_failed));
                }
            } catch (WebException e) {
                loginResult.postValue(new LoginResult(e.getErrorResource()));
            }
        };
    }

    public Runnable checkIfLoggedIn() {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.getLoggedUser();
                if (loggedUser.isPresent()) {
                    UserView userView = loggedUser.get();
                    loginResult.postValue(new LoginResult(userView));
                }
            } catch (WebException ignored) {
            }
        };
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

}