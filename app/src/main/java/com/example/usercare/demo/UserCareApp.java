package com.example.usercare.demo;

import android.app.Application;

import com.usercare.events.EventsTracker;

public class UserCareApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EventsTracker.init(this, "YOUR EVENTS API KEY HERE");
    }

}