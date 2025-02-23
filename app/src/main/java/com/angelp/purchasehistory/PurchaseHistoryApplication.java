package com.angelp.purchasehistory;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.lifecycle.MutableLiveData;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.HiltAndroidApp;
import lombok.Getter;

@HiltAndroidApp
public class PurchaseHistoryApplication extends Application {
    @Getter
    private static PurchaseHistoryApplication instance;
    @Getter
    public MutableLiveData<UserView> loggedUser = new MutableLiveData<>();
    @Getter
    public MutableLiveData<String> userToken = new MutableLiveData<>();

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        initializeJWT();
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
    }

    private void initializeJWT() {
        SharedPreferences player = getContext().getSharedPreferences("player", MODE_PRIVATE);
        userToken.setValue(player.getString("jwt", null));
    }

    public void alert(String text) {
        instance.getApplicationContext().getMainExecutor().execute(
                () -> Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show());

    }

    public void alert(int resource) {
        instance.getApplicationContext().getMainExecutor().execute(
                () -> Toast.makeText(getContext(), resource, Toast.LENGTH_SHORT).show());

    }

}
