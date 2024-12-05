package com.angelp.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.UserDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.angelp.purchasehistory.R;
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
        try (Response res = get(BACKEND_URL + "/users/self/referral-link")) {
            if (res.isSuccessful() && res.body() != null) {
                String token = res.body().string();
                return Optional.of(token);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public UserView editUser(UserDTO user) {
        try (Response res = put(BACKEND_URL + "/users/self/edit", user)) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.i("httpResponse", "register: " + json);
                return gson.fromJson(json, UserView.class);
            }
        } catch (IOException e) {
            Log.e("registerResult", "failed:" + e.getMessage());
            throw new WebException(R.string.server_connection_failed_500);
        }
        return null;
    }
}
