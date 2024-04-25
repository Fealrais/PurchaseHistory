package com.example.purchasehistory.web.clients;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.UserVO;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

public class AuthClient extends HttpClient {

    @Inject
    public AuthClient() {
    }

    private static void saveLoggedUser(String authorization) {
        SharedPreferences player = PurchaseHistoryApplication.getContext().getSharedPreferences("player", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = player.edit();
        editor.putString("jwt", authorization);
        Log.i("player_auth", authorization);
        editor.apply();
    }

    public Optional<UserView> login(String username, String password) {
        UserView result = null;
        try (Response res = post(BACKEND_URL + "/login", new UserVO(username, password, ""))) {
            String authorization = res.header("Authorization");
            if (res.isSuccessful() && res.body() != null && authorization != null && !authorization.isEmpty()) {
                String body = res.body().string();
                Log.i("loginResult", "login: " + body);
                result = gson.fromJson(body, UserView.class);
                saveLoggedUser(authorization);
                PurchaseHistoryApplication.getInstance().getLoggedUser().postValue(result);
                PurchaseHistoryApplication.getInstance().getUserToken().postValue(authorization);
            }
        } catch (IOException ignored) {
        }
        return Optional.ofNullable(result);
    }

    public Optional<UserView> getLoggedUser() {
        UserView result = null;
        try (Response res = get(BACKEND_URL + "/users/self/get")) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.i("httpResponse", "register: " + json);
                result = gson.fromJson(json, UserView.class);
                PurchaseHistoryApplication.getInstance().getLoggedUser().postValue(result);
                Log.d("jwt_valid", "JWT Valid, user is logged in");
            }
        } catch (IOException ignored) {
            Log.d("no_user", "JWT is invalid or missing. Redirected to login");
        }
        return Optional.ofNullable(result);
    }

    public void logout() {
        SharedPreferences player = PurchaseHistoryApplication.getContext().getSharedPreferences("player", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = player.edit();
        editor.clear(); //clear all stored data
        editor.apply();
    }

    public Optional<UserView> register(String username, String password, String email) {
        UserView result = null;
        try (Response res = post(BACKEND_URL + "/register", new UserVO(username, password, email))) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.i("httpResponse", "register: " + json);
                return Optional.of(gson.fromJson(json, UserView.class));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(result);
    }
}
