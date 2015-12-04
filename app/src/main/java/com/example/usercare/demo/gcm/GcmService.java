package com.example.usercare.demo.gcm;

import com.google.android.gms.gcm.GcmListenerService;

import com.usercare.gcm.UserCareGcmHandler;

import android.os.Bundle;

public class GcmService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        UserCareGcmHandler.init(getApplicationContext()).onMessageReceived(data);
    }

}