package com.example.purchasehistory.web.clients;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserClient extends HttpClient {
    @Inject
    public UserClient() {
    }
//
//    public PassedLevelView changeLevel(Long levelId) {
//        try (Response res = post(BACKEND_URL + "/levels/change?levelId=" + levelId, null)) {
//            ResponseBody body = res.body();
//            if (res.isSuccessful() && body != null) {
//                String json = body.string();
//                Log.i("httpResponse", "changeLevel: " + json);
//                return gson.fromJson(json, PassedLevelView.class);
//            } else throw new IOException("Failed to initialize game");
//        } catch (IOException ignored) {
//        }
//        return null;
//    }
}
