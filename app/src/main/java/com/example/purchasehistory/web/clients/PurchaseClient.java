package com.example.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseSubmit;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.data.model.PurchaseResponse;
import com.google.gson.JsonParseException;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class PurchaseClient extends HttpClient {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    public PurchaseClient() {
    }

    public PurchaseView createPurchase(String qrContent) {
        try (Response res = post(BACKEND_URL + "/purchase", new PurchaseSubmit(qrContent))) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Create Purchase: " + json);
                return gson.fromJson(json, PurchaseResponse.class).toPurchaseView();
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }

    public List<PurchaseView> getAllPurchases() throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase")) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful())
                    return Arrays.stream(gson.fromJson(json, PurchaseResponse[].class)).map(PurchaseResponse::toPurchaseView).collect(Collectors.toList());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }
}
