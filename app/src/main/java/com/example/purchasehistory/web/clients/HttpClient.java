package com.example.purchasehistory.web.clients;

import android.util.Log;
import android.widget.Toast;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.web.interceptors.AuthInterceptor;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class HttpClient {
    public static String HOST_NAME = "192.168.0.104";
    public static String BACKEND_URL = "https://" + HOST_NAME + ":9000/api";
    protected final OkHttpClient client;
    protected final AuthInterceptor authInterceptor = new AuthInterceptor();
    protected final Gson gson = new Gson();

    public HttpClient() {
        client = new OkHttpClient().newBuilder()
                .hostnameVerifier((hostname, session) -> true)
                .addInterceptor(authInterceptor)
                .build();
    }

    public Response get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).
                execute();
        handleError(response);
        return response;
    }

    public Response post(String url, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        handleError(response);
        return response;
    }

    private void handleError(Response response) {
        if (response.code() == 401) {
            PurchaseHistoryApplication.getInstance().getUserToken().postValue(null);
        } else if (response.code() == 400 && response.body() != null) {
            try {
                String json = response.body().string();
                ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                Log.i("ErrorHandled", "handleError: " + errorResponse);
                PurchaseHistoryApplication.getContext().getMainExecutor().execute(() -> Toast.makeText(PurchaseHistoryApplication.getContext(), errorResponse.getDetail(), Toast.LENGTH_SHORT).show());
            } catch (IOException ignored) {
            }
        }
    }
}
