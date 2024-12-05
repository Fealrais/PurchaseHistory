package com.angelp.purchasehistory.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.model.LoginResult;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;
import java.util.Locale;
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

    public Runnable register(String username, String password, String email, Locale locale) {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.register(username, password, email, locale);

                if (loggedUser.isPresent()) {
                    UserView userView = loggedUser.get();
                    registerResult.postValue(new LoginResult(userView));
                } else {
                    registerResult.postValue(new LoginResult(R.string.register_failed));
                }
            } catch (WebException e) {
                registerResult.postValue(new LoginResult(e.getErrorResource()));
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
        if (!AndroidUtils.isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null, null));
        } else if (!AndroidUtils.isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password, null));
        } else if (!AndroidUtils.isEmailValid(email)) {
            registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_email));
        } else {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }



}