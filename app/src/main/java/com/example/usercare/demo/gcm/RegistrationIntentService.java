package com.example.usercare.demo.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.usercare.gcm.UserCareGcmHandler;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();
	/**
     * AgentAI sender ID, it need for recieve GCM form agent server. Don't change it.
     */
    public static final String AGENT_GCM_SENDER_ID = "306163195069";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        UserCareGcmHandler.init(getApplicationContext()).setGcmSenderId(AGENT_GCM_SENDER_ID);
    }
}