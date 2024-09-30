package com.example.purchasehistory.web.clients;

import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class ObserverClient extends HttpClient {
    private static final String OBSERVER_ENPOINT = BACKEND_URL + "/observer";

    @Inject
    public ObserverClient() {
    }

    public List<UserView> getObservedUsers() throws RuntimeException {
        try (Response res = get(OBSERVER_ENPOINT + "/users")) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Observed users: " + json);
                if (res.isSuccessful())
                    return gson.fromJson(json, new TypeToken<List<UserView>>() {
                    }.getType());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e("httpResponseError", "getObservedUsers ERROR: " + e.getMessage());
        }
        return null;
    }

}
