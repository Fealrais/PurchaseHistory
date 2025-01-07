package com.angelp.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.data.filters.PageRequest;
import com.angelp.purchasehistory.web.LocalDateGsonAdapter;
import com.angelp.purchasehistory.web.interceptors.AuthInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;

public class HttpClient {
    public static String HOST_NAME = "https://angelp-home.duckdns.org";
    public static String BACKEND_URL = HOST_NAME + ":9000/api";
    protected final OkHttpClient client;
    protected final AuthInterceptor authInterceptor = new AuthInterceptor();
    protected final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateGsonAdapter()).create();
    private final String TAG = this.getClass().getSimpleName();

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
        Log.i(TAG, String.format("Sending GET: '%s'", url));
        Response response = client.newCall(request).
                execute();
        Log.i(TAG, String.format("Received response GET: '%s' ", url));
        handleError(response);
        return response;
    }

    public Response get(String url, PageRequest pageRequest) throws IOException {
        Request request = new Request.Builder()
                .url(pageRequest.buildURL(url))
                .get()
                .build();
        Log.i(TAG, String.format("Sending GET: '%s'", url));
        Response response = client.newCall(request).
                execute();
        Log.i(TAG, String.format("Received response GET: '%s' ", url));
        handleError(response);
        return response;
    }

    public Response post(String url, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), json);
        Log.i(TAG, String.format("Sending POST: '%s' Body:%s", url, json));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        Log.i(TAG, String.format("Received response POST: '%s' ", url));
        handleError(response);
        return response;
    }

    public Response put(String url, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), json);
        Log.i(TAG, String.format("Sending PUT: '%s' Body:%s", url, json));
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        Log.i(TAG, String.format("Received response PUT: '%s' ", url));
        handleError(response);
        return response;
    }

    public Response delete(String url) throws IOException {
        Log.i(TAG, String.format("Sending DELETE: '%s'", url));
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        Response response = client.newCall(request).execute();
        Log.i(TAG, String.format("Received response DELETE: '%s' ", url));
        handleError(response);
        return response;
    }

    public Response postFormData(String url, RequestBody body) throws IOException {
        Log.i(TAG, String.format("Sending POST : %s", url));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        Log.i(TAG, String.format("Received response POST: '%s' ", url));
        handleError(response);
        return response;
    }

    private void handleError(Response response) {
        if (response.code() == 401) {
            PurchaseHistoryApplication.getInstance().getUserToken().postValue(null);
        }
    }
}
