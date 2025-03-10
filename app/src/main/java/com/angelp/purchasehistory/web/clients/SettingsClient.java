package com.angelp.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.MonthlyLimitDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SettingsClient extends HttpClient {
    @Inject
    public SettingsClient() {
    }

    public MonthlyLimitView addMonthlyLimit(MonthlyLimitDTO monthlyLimit) {
        try (Response res = post(BACKEND_URL + "/monthly-limit", monthlyLimit)) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.i("httpResponse", "addMonthlyLimit: " + json);
                return gson.fromJson(json, MonthlyLimitView.class);
            }
        } catch (IOException e) {
            Log.e("addMonthlyLimit", "ERROR: " + e.getMessage());
        }
        return null;
    }

    public List<MonthlyLimitView> getMonthlyLimits() {
        try (Response res = get(BACKEND_URL + "/monthly-limit")) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.i("httpResponse", "getMonthlyLimits: " + json);
                return Arrays.stream(gson.fromJson(json, MonthlyLimitView[].class)).collect(Collectors.toList());
            }
        } catch (IOException e) {
            Log.e("getMonthlyLimits", "ERROR: " + e.getMessage());
        }
        return null;
    }

    public MonthlyLimitView updateMonthlyLimit(Long id, MonthlyLimitDTO monthlyLimit) {
        try (Response res = put(BACKEND_URL + "/monthly-limit/" + id, monthlyLimit)) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.i("httpResponse", "updateMonthlyLimit: " + json);
                return gson.fromJson(json, MonthlyLimitView.class);
            }
        } catch (IOException e) {
            Log.e("updateMonthlyLimit", "ERROR: " + e.getMessage());
        }
        return null;
    }

    public void deleteMonthlyLimit(Long id) {
        try (Response res = delete(BACKEND_URL + "/monthly-limit/" + id)) {
            if (res.isSuccessful()) {
                Log.i("httpResponse", "deleteMonthlyLimit: Success");
            } else {
                Log.e("deleteMonthlyLimit", "Failed to delete monthly limit");
            }
        } catch (IOException e) {
            Log.e("deleteMonthlyLimit", "ERROR: " + e.getMessage());
        }
    }
}