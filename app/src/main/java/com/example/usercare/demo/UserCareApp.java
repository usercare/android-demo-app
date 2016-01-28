package com.example.usercare.demo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.usercare.UserCare;

import io.fabric.sdk.android.Fabric;

public class UserCareApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        UserCare.init(this)
                .withEventsTracker("YOUR EVENTS API KEY HERE")
                .withExceptionHandler();
    }

}