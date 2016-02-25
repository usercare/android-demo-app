package com.example.usercare.demo.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.usercare.gcm.UserCareGcmHandler;

public class GcmService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        UserCareGcmHandler.init(getApplicationContext()).onMessageReceived(data);
    }

}