package com.example.usercare.demo.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import com.usercare.utils.UserCareUtils;

import android.content.Context;
import android.content.Intent;

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