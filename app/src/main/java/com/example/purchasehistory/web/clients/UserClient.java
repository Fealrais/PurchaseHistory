package com.example.purchasehistory.web.clients;

import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class UserClient extends HttpClient {
    @Inject
    public UserClient() {
    }
    public Optional<String> getReferralToken() {
        String OBSERVER_ADD_USER_ENDPOINT = BACKEND_URL+"observer/users/add?token=";
        try (Response res = get(BACKEND_URL + "/users/self/referral-link")) {
            if (res.isSuccessful() && res.body() != null) {
                String token = res.body().string();
                return Optional.of(OBSERVER_ADD_USER_ENDPOINT + token);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}
