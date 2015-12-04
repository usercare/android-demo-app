package com.example.usercare.demo.gcm;

import com.usercare.gcm.UserCareGcmHandler;

import android.app.IntentService;
import android.content.Intent;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private static final String GCM_SENDER_ID = "INPUT YOUR GCM SENDER ID";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        UserCareGcmHandler.init(getApplicationContext()).setGcmSenderId(GCM_SENDER_ID);
    }

}