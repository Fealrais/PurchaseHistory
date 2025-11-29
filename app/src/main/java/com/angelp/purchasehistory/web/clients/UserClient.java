package com.angelp.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistorybackend.models.views.incoming.ErrorFeedback;
import com.angelp.purchasehistorybackend.models.views.incoming.UpdatePasswordDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.UserDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserAnalytics;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
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

    public boolean deleteAccount() {
        try (Response res = delete(BACKEND_URL + "/users/self")) {
            return res.isSuccessful();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UserView updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        try (Response res = put(BACKEND_URL + "/users/self/password", updatePasswordDTO)) {
            return utils.getBody(res, UserView.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFeedback(ErrorFeedback errorFeedback) {
        try (Response res = post(BACKEND_URL + "/feedback/error", errorFeedback)) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UserAnalytics getUserAnalytics(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/users/self/analytics?" + filter)) {
            return utils.getBody(res, UserAnalytics.class);
        } catch (WebException e) {
            PurchaseHistoryApplication.getInstance().alert(R.string.failed);
            return new UserAnalytics();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
