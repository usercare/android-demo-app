package com.example.usercare.demo.gcm;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.usercare.UserCare;
import com.usercare.utils.SdkNetworkUtils;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private SdkNetworkUtils sdkNetworkUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        sdkNetworkUtils = UserCare.getInstance().getDependencyManager().getSingleAppComponent().getSdkNetworkUtils();
    }

    @Override
    public void onTokenRefresh() {
        Context context = getApplicationContext();
        if (sdkNetworkUtils.isOnline()) {
            Intent intent = new Intent(context, RegistrationIntentService.class);
            startService(intent);
        }
    }
}