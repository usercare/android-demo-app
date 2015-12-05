package com.example.usercare.demo.gcm;

import android.content.Context;
import android.content.Intent;

import com.example.usercare.demo.UserCareUtils;
import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Context context = getApplicationContext();
        if (UserCareUtils.isOnline(context)) {
            Intent intent = new Intent(context, RegistrationIntentService.class);
            startService(intent);
        }
    }

}