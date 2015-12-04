package com.example.usercare.demo;

import com.usercare.events.EventsTracker;

import android.app.Application;

public class UserCareApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EventsTracker.init(this, "YOUR EVENTS API KEY HERE");
    }

}