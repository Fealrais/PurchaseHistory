package com.example.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseSubmit;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class PurchaseClient extends HttpClient {
    @Inject
    public PurchaseClient() {
    }

    public PurchaseView createPurchase(String qrContent) {
        try (Response res = post(BACKEND_URL + "/purchase", new PurchaseSubmit(qrContent))) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "changeLevel: " + json);
                return gson.fromJson(json, PurchaseView.class);
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }
}
